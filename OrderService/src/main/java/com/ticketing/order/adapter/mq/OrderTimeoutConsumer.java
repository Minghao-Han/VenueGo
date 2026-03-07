package com.ticketing.order.adapter.mq;

import com.ticketing.order.app.service.OrderCommandService;
import com.ticketing.order.common.config.AppProperties;
import com.ticketing.order.infrastructure.idempotent.IdempotentChecker;
import com.ticketing.order.infrastructure.payment.PaymentQueryService;
import com.ticketing.order.infrastructure.repository.OrderRepository;
import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderEvent;
import com.ticketing.order.domain.order.enums.OrderStatus;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Optional;

/**
 * RocketMQ consumer for payment timeout messages
 * 
 * Receives timeout messages after the delay period and processes them:
 * 1. Check idempotency
 * 2. Query payment platform to detect any late payments
 * 3. Fire appropriate event (TIMEOUT or PAY if detected)
 * 4. Update order state with optimistic lock
 */
@Component
@Slf4j
public class OrderTimeoutConsumer {

    private final OrderRepository orderRepository;
    private final OrderCommandService orderCommandService;
    private final IdempotentChecker idempotentChecker;
    private final PaymentQueryService paymentQueryService;
    private final AppProperties appProperties;

    private DefaultMQPushConsumer consumer;

    public OrderTimeoutConsumer(
            OrderRepository orderRepository,
            OrderCommandService orderCommandService,
            IdempotentChecker idempotentChecker,
            PaymentQueryService paymentQueryService,
            AppProperties appProperties) {
        this.orderRepository = orderRepository;
        this.orderCommandService = orderCommandService;
        this.idempotentChecker = idempotentChecker;
        this.paymentQueryService = paymentQueryService;
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() throws Exception {
        String nameServer = appProperties.getNameServer();

        consumer = new DefaultMQPushConsumer(appProperties.getTimeoutConsumerGroup());
        consumer.setNamesrvAddr(nameServer);
        consumer.subscribe(appProperties.getTimeoutTopic(), "*");
        consumer.setConsumeThreadMin(1);
        consumer.setConsumeThreadMax(10);

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    processTimeoutMessage(msg);
                } catch (Exception e) {
                    log.error("Error processing timeout message", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        log.info("OrderTimeoutConsumer started with nameServer: {}", nameServer);
    }

    @PreDestroy
    public void cleanup() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("OrderTimeoutConsumer shutdown");
        }
    }

    private void processTimeoutMessage(MessageExt message) {
        String orderId = new String(message.getBody());

        try {
            log.info("Received timeout message for order: {}", orderId);

            // Step 1: Check idempotency key in Redis
            String idempotentKey = "timeout:" + orderId;
            if (!this.idempotentChecker.checkAndMarkAsProcessed(idempotentKey)) {
                log.warn("Timeout message already processed for order: {}", orderId);
                return;
            }

            // Step 2: Load order from DB (including version)
            Optional<OrderAggregate> optionalOrder = this.orderRepository.findById(orderId);
            if (optionalOrder.isEmpty()) {
                log.error("Order not found: {}", orderId);
                return;
            }

            OrderAggregate order = optionalOrder.get();

            // Step 3: Early exit if order is not in PENDING_PAY
            if (!OrderStatus.PENDING_PAY.equals(order.getStatus())) {
                log.info("Order already paid or cancelled: {} - status: {}", orderId, order.getStatus());
                return;
            }

            // Step 4: Query payment platform to check actual payment status
            PaymentQueryService.PaymentStatus paymentStatus = queryPaymentStatus(orderId);

            OrderEvent eventToFire;
            String reason;

            if (PaymentQueryService.PaymentStatus.PAID.equals(paymentStatus)) {
                log.info("Payment detected for timed out order: {}", orderId);
                eventToFire = OrderEvent.PAY;
                reason = "Late payment detected during timeout processing";
            } else {
                log.info("Payment timeout confirmed for order: {}", orderId);
                eventToFire = OrderEvent.TIMEOUT;
                reason = "Payment timeout after " + appProperties.getPayTimeoutMinutes() + " minutes";
            }

            // Step 5: Fire event through state machine and execute command
            this.orderCommandService.executeStateTransition(
                    orderId,
                    order.getVersion(),
                    eventToFire,
                    reason);

            log.info("Timeout message processed successfully for order: {}", orderId);

        } catch (Exception e) {
            log.error("Error processing timeout message for order: {}", orderId, e);
            throw new RuntimeException("Timeout processing failed", e);
        }
    }

    private PaymentQueryService.PaymentStatus queryPaymentStatus(String orderId) {
        try {
            return this.paymentQueryService.queryPaymentStatus(orderId);
        } catch (PaymentQueryService.PaymentQueryException e) {
            log.error("Payment query failed for order: {}", orderId, e);
            throw new RuntimeException("Payment query failed", e);
        }
    }
}
