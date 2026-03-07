package com.ticketing.order.infrastructure.idempotent.impl;

import com.ticketing.order.common.config.AppProperties;
import com.ticketing.order.infrastructure.idempotent.IdempotentChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based idempotency checker
 * Uses Redis to store processed idempotency keys
 */
@Component
public class RedisIdempotentChecker implements IdempotentChecker {

    private static final Logger log = LoggerFactory.getLogger(RedisIdempotentChecker.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    public RedisIdempotentChecker(RedisTemplate<String, String> redisTemplate, AppProperties appProperties) {
        this.redisTemplate = redisTemplate;
        this.appProperties = appProperties;
    }

    @Override
    public boolean isProcessed(String idempotentKey) {
        String fullKey = buildFullKey(idempotentKey);
        Boolean exists = this.redisTemplate.hasKey(fullKey);
        return exists != null && exists;
    }

    @Override
    public void markAsProcessed(String idempotentKey) {
        String fullKey = buildFullKey(idempotentKey);
        long ttlSeconds = this.appProperties.getIdempotentKeyTtlSeconds();
        this.redisTemplate.opsForValue().set(
                fullKey,
                "1",
                ttlSeconds,
                TimeUnit.SECONDS);
        log.debug("Marked idempotency key as processed: {}", idempotentKey);
    }

    @Override
    public boolean checkAndMarkAsProcessed(String idempotentKey) {
        String fullKey = buildFullKey(idempotentKey);
        long ttlSeconds = this.appProperties.getIdempotentKeyTtlSeconds();

        // Use Redis SETEX with GET for atomic check-and-set
        // If key doesn't exist, SET it and return nil (no previous value)
        // If key exists, SET it again and return the previous value
        Object prevValue = this.redisTemplate.opsForValue().getAndSet(
                fullKey,
                "1");

        // Set expiration
        this.redisTemplate.expire(fullKey, ttlSeconds, TimeUnit.SECONDS);

        boolean isFirstTime = prevValue == null;

        if (isFirstTime) {
            log.debug("Idempotency key processed for the first time: {}", idempotentKey);
        } else {
            log.warn("Idempotency key already processed: {}", idempotentKey);
        }

        return isFirstTime;
    }

    /**
     * Build full Redis key with prefix
     */
    private String buildFullKey(String idempotentKey) {
        return this.appProperties.getIdempotentKeyPrefix() + idempotentKey;
    }
}
