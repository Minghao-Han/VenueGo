package com.ticketing.order.adapter.mq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.order.app.command.CreateOrderCmd;
import com.ticketing.order.app.service.OrderCommandService;
import com.ticketing.order.common.config.AppProperties;
import com.ticketing.order.infrastructure.grpc.VenueTicketTierQueryClient;
import com.ticketing.order.infrastructure.idempotent.IdempotentChecker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.UUID;

/**
 * Consumer that asynchronously creates orders from Ticketing purchase events.
 */
@Component
@Slf4j
public class TicketPurchaseConsumer {

    private final OrderCommandService orderCommandService;
    private final IdempotentChecker idempotentChecker;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final VenueTicketTierQueryClient venueTicketTierQueryClient;

    private DefaultMQPushConsumer consumer;

    public TicketPurchaseConsumer(
            OrderCommandService orderCommandService,
            IdempotentChecker idempotentChecker,
            AppProperties appProperties,
            ObjectMapper objectMapper,
            VenueTicketTierQueryClient venueTicketTierQueryClient) {
        this.orderCommandService = orderCommandService;
        this.idempotentChecker = idempotentChecker;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.venueTicketTierQueryClient = venueTicketTierQueryClient;
    }

    @PostConstruct
    public void init() throws Exception {
        String nameServer = appProperties.getNameServer();

        consumer = new DefaultMQPushConsumer(appProperties.getPurchaseConsumerGroup());
        consumer.setNamesrvAddr(nameServer);
        consumer.subscribe(appProperties.getPurchaseTopic(), "*");
        consumer.setConsumeThreadMin(1);
        consumer.setConsumeThreadMax(10);

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    processPurchase(msg);
                } catch (Exception e) {
                    log.error("Error processing ticket purchase message", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        log.info("TicketPurchaseConsumer started with nameServer: {}", nameServer);
    }

    @PreDestroy
    public void cleanup() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("TicketPurchaseConsumer shutdown");
        }
    }

    private void processPurchase(MessageExt message) throws Exception {
        String body = new String(message.getBody());
        TicketPurchaseEvent payload = objectMapper.readValue(body, TicketPurchaseEvent.class);

        String messageId = payload.getMessageId() != null ? payload.getMessageId() : message.getMsgId();
        if (messageId == null || messageId.isBlank()) {
            messageId = payload.getOrderId() == null ? "unknown" : payload.getOrderId().toString();
        }

        String idempotentKey = "purchase:" + messageId;
        if (!this.idempotentChecker.checkAndMarkAsProcessed(idempotentKey)) {
            log.warn("Purchase message already processed: {}", messageId);
            return;
        }

        if (payload.getOrderId() == null || payload.getTicketTierId() == null || payload.getPurchaseCount() == null) {
            throw new IllegalArgumentException("Invalid purchase payload: missing required fields");
        }

        VenueTicketTierQueryClient.TicketTierInfo ticketTierInfo = this.venueTicketTierQueryClient
            .queryTicketTierInfo(payload.getTicketTierId());

        CreateOrderCmd cmd = new CreateOrderCmd(
                "async-event",
            ticketTierInfo.venueId().toString(),
                payload.getTicketTierId().toString(),
                payload.getPurchaseCount(),
            ticketTierInfo.price());

        orderCommandService.createOrderWithOrderId(payload.getOrderId().toString(), cmd);
        log.info("Async order created from purchase event, orderId={}, ticketTierId={}, qty={}",
                payload.getOrderId(), payload.getTicketTierId(), payload.getPurchaseCount());
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TicketPurchaseEvent {
        private String messageId;
        private UUID orderId;
        private UUID userId;
        private UUID ticketTierId;
        private Integer purchaseCount;
    }
}
