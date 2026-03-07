package com.ticketing.order.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application configuration properties bound to app.* prefix in YAML
 * All environment-specific values are externalized here - no hardcoding in code
 */
@Component
@ConfigurationProperties(prefix = "app.order")
public class AppProperties {

    // Payment timeout configuration
    private Integer payTimeoutMinutes = 15;

    // RocketMQ topic and consumer group configuration
    private String timeoutTopic = "order_timeout_topic";
    private String timeoutConsumerGroup = "order-timeout-consumer-group";
    private String payCallbackTopic = "pay_callback_topic";
    private String payCallbackConsumerGroup = "pay-callback-consumer-group";

    // Redis idempotency configuration
    private Integer idempotentKeyTtlSeconds = 86400;
    private String idempotentKeyPrefix = "order:idempotent:";

    // Optimistic lock retry configuration
    private Integer optimisticLockMaxRetries = 3;

    // Nested payment properties
    private Payment payment = new Payment();

    public Integer getPayTimeoutMinutes() {
        return payTimeoutMinutes;
    }

    public void setPayTimeoutMinutes(Integer payTimeoutMinutes) {
        this.payTimeoutMinutes = payTimeoutMinutes;
    }

    public String getTimeoutTopic() {
        return timeoutTopic;
    }

    public void setTimeoutTopic(String timeoutTopic) {
        this.timeoutTopic = timeoutTopic;
    }

    public String getTimeoutConsumerGroup() {
        return timeoutConsumerGroup;
    }

    public void setTimeoutConsumerGroup(String timeoutConsumerGroup) {
        this.timeoutConsumerGroup = timeoutConsumerGroup;
    }

    public String getPayCallbackTopic() {
        return payCallbackTopic;
    }

    public void setPayCallbackTopic(String payCallbackTopic) {
        this.payCallbackTopic = payCallbackTopic;
    }

    public String getPayCallbackConsumerGroup() {
        return payCallbackConsumerGroup;
    }

    public void setPayCallbackConsumerGroup(String payCallbackConsumerGroup) {
        this.payCallbackConsumerGroup = payCallbackConsumerGroup;
    }

    public Integer getIdempotentKeyTtlSeconds() {
        return idempotentKeyTtlSeconds;
    }

    public void setIdempotentKeyTtlSeconds(Integer idempotentKeyTtlSeconds) {
        this.idempotentKeyTtlSeconds = idempotentKeyTtlSeconds;
    }

    public String getIdempotentKeyPrefix() {
        return idempotentKeyPrefix;
    }

    public void setIdempotentKeyPrefix(String idempotentKeyPrefix) {
        this.idempotentKeyPrefix = idempotentKeyPrefix;
    }

    public Integer getOptimisticLockMaxRetries() {
        return optimisticLockMaxRetries;
    }

    public void setOptimisticLockMaxRetries(Integer optimisticLockMaxRetries) {
        this.optimisticLockMaxRetries = optimisticLockMaxRetries;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    // Deprecated method - use getPayment() instead
    public Payment getOrder() {
        return this.payment;
    }

    public static class Payment {
        private String queryUrl = "http://localhost:8080/payment/query";
        private Integer queryTimeoutMs = 3000;

        public String getQueryUrl() {
            return queryUrl;
        }

        public void setQueryUrl(String queryUrl) {
            this.queryUrl = queryUrl;
        }

        public Integer getQueryTimeoutMs() {
            return queryTimeoutMs;
        }

        public void setQueryTimeoutMs(Integer queryTimeoutMs) {
            this.queryTimeoutMs = queryTimeoutMs;
        }
    }
}
