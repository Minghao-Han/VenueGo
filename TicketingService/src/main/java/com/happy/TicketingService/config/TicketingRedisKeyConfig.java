package com.happy.TicketingService.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ticketing.redis.key")
public class TicketingRedisKeyConfig {

    private String inventoryPrefix;
    private String infoPrefix;
    private String orderedPrefix;
}
