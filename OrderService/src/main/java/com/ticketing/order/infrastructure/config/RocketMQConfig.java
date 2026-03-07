package com.ticketing.order.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RocketMQ
 * Properties are loaded from application.yaml via rocketmq.* prefix
 * RocketMQ Spring Boot starter automatically configures the producer and consumer
 */
@Configuration
public class RocketMQConfig {
    // RocketMQ configuration is handled by spring-boot-starter-rocketmq
    // Configuration comes from application.yaml properties
}
