package com.ticketing.order.adapter.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.order.app.service.OrderCommandService;
import com.ticketing.order.infrastructure.idempotent.IdempotentChecker;
import com.ticketing.order.infrastructure.repository.OrderRepository;
import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.ticketing.order.common.config.AppProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * RocketMQ consumer for payment callback messages
 * 
 * Receives payment success notifications from the payment platform.
 * Handles state transitions properly:
 * - PENDING_PAY + PAY -> PAID (normal case)
 * - TIMEOUT_CANCELLED + PAY -> REFUNDING (late payment after timeout)
 * - CANCELLED + PAY -> REFUNDING (late payment after user cancellation)
 */
@Component
@Slf4j
public class PayCallbackConsumer {

    private final OrderRepository orderRepository;
    private final OrderCommandService orderCommandService;
    private final IdempotentChecker idempotentChecker;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    private DefaultMQPushConsumer consumer;

    public PayCallbackConsumer(
            OrderRepository orderRepository,
            OrderCommandService orderCommandService,
            IdempotentChecker idempotentChecker,
            AppProperties appProperties,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderCommandService = orderCommandService;
        this.idempotentChecker = idempotentChecker;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws Exception {
        String nameServer = appProperties.getNameServer();

        consumer = new DefaultMQPushConsumer(appProperties.getPayCallbackConsumerGroup());
        consumer.setNamesrvAddr(nameServer);
        consumer.subscribe(appProperties.getPayCallbackTopic(), "*");
        consumer.setConsumeThreadMin(1);
        consumer.setConsumeThreadMax(10);

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    processPaymentCallback(msg);
                } catch (Exception e) {
                    log.error("Error processing payment callback", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        log.info("PayCallbackConsumer started with nameServer: {}", nameServer);
    }

    @PreDestroy
    public void cleanup() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("PayCallbackConsumer shutdown");
        }
    }

    private void processPaymentCallback(MessageExt message) {
        try {
            String body = new String(message.getBody());
            PaymentCallback callback = objectMapper.readValue(body, PaymentCallback.class);

            log.info("Received payment callback: paymentId={}, orderId={}, amount={}",
                    callback.getPaymentId(),
                    callback.getOrderId(),
                    callback.getAmount());

            String orderId = callback.getOrderId();
            String paymentId = callback.getPaymentId();

            // Step 1: Check idempotency key in Redis using paymentId
            String idempotentKey = "payment:" + paymentId;
            if (!this.idempotentChecker.checkAndMarkAsProcessed(idempotentKey)) {
                log.warn("Payment callback already processed: {}", paymentId);
                return;
            }

            // Step 2: Load order from DB (including version)
            Optional<OrderAggregate> optionalOrder = this.orderRepository.findById(orderId);
            if (optionalOrder.isEmpty()) {
                log.error("Order not found for payment callback: {}", orderId);
                return;
            }

            OrderAggregate order = optionalOrder.get();

            log.debug("Current order status: {} for payment callback: {}", order.getStatus(), paymentId);

            // Step 3: Fire PAY event through state machine
            this.orderCommandService.executePaymentTransition(
                    orderId,
                    order.getVersion(),
                    paymentId,
                    callback.getAmount());

            log.info("Payment callback processed successfully: paymentId={}, orderId={}",
                    paymentId,
                    orderId);

        } catch (Exception e) {
            log.error("Error processing payment callback message", e);
            throw new RuntimeException("Payment callback processing failed", e);
        }
    }

    /**
     * DTO for payment callback message
     */
    @Data
    public static class PaymentCallback {
        private String paymentId;
        private String orderId;
        private BigDecimal amount;
    }
}
