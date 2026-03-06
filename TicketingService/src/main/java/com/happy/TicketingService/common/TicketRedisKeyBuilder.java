package com.happy.TicketingService.common;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.happy.TicketingService.config.TicketingRedisKeyConfig;

@Component
public class TicketRedisKeyBuilder {

    private final TicketingRedisKeyConfig redisKeyConfig;

    public TicketRedisKeyBuilder(TicketingRedisKeyConfig redisKeyConfig) {
        this.redisKeyConfig = redisKeyConfig;
    }

    public String buildInventoryKey(UUID ticketTierId) {
        return redisKeyConfig.getInventoryPrefix() + ticketTierId;
    }

    public String buildInfoKey(UUID ticketTierId) {
        return redisKeyConfig.getInfoPrefix() + ticketTierId;
    }

    public String buildOrderedKey(UUID ticketTierId, UUID userId) {
        return redisKeyConfig.getOrderedPrefix() + ticketTierId + ":" + userId;
    }
}
