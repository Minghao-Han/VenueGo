package com.ticketing.order.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application configuration properties bound to app.* prefix in YAML
 * All environment-specific values are externalized here - no hardcoding in code
 */
@Component
@Data
@ConfigurationProperties(prefix = "app.order")
public class AppProperties {

    // Payment timeout configuration
    private Integer payTimeoutMinutes = 15;

    // RocketMQ topic and consumer group configuration
    private String timeoutTopic = "order_timeout_topic";
    private String timeoutConsumerGroup = "order-timeout-consumer-group";
    private String payCallbackTopic = "pay_callback_topic";
    private String payCallbackConsumerGroup = "pay-callback-consumer-group";
    private String purchaseTopic = "ticketing-purchase-success";
    private String purchaseConsumerGroup = "ticketing-order-consumer";
    private String nameServer = "localhost:9876";

    // Redis idempotency configuration
    private Integer idempotentKeyTtlSeconds = 86400;
    private String idempotentKeyPrefix = "order:idempotent:";

    // Optimistic lock retry configuration
    private Integer optimisticLockMaxRetries = 3;

    // Nested payment properties
    private Payment payment = new Payment();

    // Deprecated method - use getPayment() instead
    public Payment getOrder() {
        return this.payment;
    }

    @Data
    public static class Payment {
        private String queryUrl = "http://localhost:8080/payment/query";
        private Integer queryTimeoutMs = 3000;
    }
}
