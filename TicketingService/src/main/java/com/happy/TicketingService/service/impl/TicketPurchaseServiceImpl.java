package com.happy.TicketingService.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.happy.TicketingService.common.TicketRedisKeyBuilder;
import com.happy.TicketingService.config.TicketingMqTopicConfig;
import com.happy.TicketingService.dto.PurchaseTicketResponse;
import com.happy.TicketingService.exception.BusinessException;
import com.happy.TicketingService.event.TicketPurchaseEvent;
import com.happy.TicketingService.mq.GeneralMessageProducer;
import com.happy.TicketingService.service.InventoryService;
import com.happy.TicketingService.service.PaymentService;
import com.happy.TicketingService.service.TicketPurchaseService;

import jakarta.annotation.PostConstruct;

@Service
public class TicketPurchaseServiceImpl implements TicketPurchaseService {

    private static final Logger log = LoggerFactory.getLogger(TicketPurchaseServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final GeneralMessageProducer messageProducer;
    private final TicketingMqTopicConfig mqTopicConfig;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final TicketRedisKeyBuilder ticketRedisKeyBuilder;

    private String purchaseLuaScript;
    private String purchaseLuaSha1;

    public TicketPurchaseServiceImpl(
            StringRedisTemplate redisTemplate,
            GeneralMessageProducer messageProducer,
            TicketingMqTopicConfig mqTopicConfig,
            PaymentService paymentService,
            InventoryService inventoryService,
            TicketRedisKeyBuilder ticketRedisKeyBuilder) {
        this.redisTemplate = redisTemplate;
        this.messageProducer = messageProducer;
        this.mqTopicConfig = mqTopicConfig;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.ticketRedisKeyBuilder = ticketRedisKeyBuilder;
    }

    @PostConstruct
    public void init() {
        this.purchaseLuaScript = readLuaScript();
        this.purchaseLuaSha1 = loadScriptToRedis(this.purchaseLuaScript.getBytes(StandardCharsets.UTF_8));
        inventoryService.prewarmInventoryCache();
    }

    @Override
    public PurchaseTicketResponse purchase(UUID userId, UUID ticketTierId, Integer purchaseCount) {
        if (purchaseCount == null || purchaseCount <= 0) {
            throw new BusinessException("purchaseCount must be greater than 0");
        }

        long purchaseStartNs = System.nanoTime();
        long redisStepNs = -1L;
        long paymentStepNs = -1L;
        long mqStepNs = -1L;
        UUID orderId = null;

        try {
            long redisStartNs = System.nanoTime();
            long result = executePurchaseScript(
                ticketRedisKeyBuilder.buildInventoryKey(ticketTierId),
                ticketRedisKeyBuilder.buildOrderedKey(ticketTierId, userId),
                ticketRedisKeyBuilder.buildInfoKey(ticketTierId),
                    purchaseCount,
                    OffsetDateTime.now().toEpochSecond());
            redisStepNs = System.nanoTime() - redisStartNs;

            if (result == 1L) {
                return PurchaseTicketResponse.builder()
                        .code(410)
                        .message("Ticket is sold out")
                        .build();
            }
            if (result == 2L) {
                return PurchaseTicketResponse.builder()
                        .code(410)
                        .message("Purchase limit exceeded")
                        .build();
            }
            if (result == 3L) {
                return PurchaseTicketResponse.builder()
                        .code(412)
                        .message("Ticket purchase is not in sale window")
                        .build();
            }
            if (result == 4L) {
                return PurchaseTicketResponse.builder()
                        .code(500)
                        .message("Ticket info missing in Redis")
                        .build();
            }

            orderId = UUID.randomUUID();

            long paymentStartNs = System.nanoTime();
            String prepayCode = paymentService.generatePrepayCode(userId, ticketTierId, orderId, purchaseCount);
            paymentStepNs = System.nanoTime() - paymentStartNs;

            TicketPurchaseEvent message = TicketPurchaseEvent.builder()
                    .messageId(UUID.randomUUID().toString())
                    .orderId(orderId)
                    .userId(userId)
                    .ticketTierId(ticketTierId)
                    .purchaseCount(purchaseCount)
                    .occurredAt(OffsetDateTime.now())
                    .build();

            long mqStartNs = System.nanoTime();
            messageProducer.sendAsync(mqTopicConfig.getPurchaseTopic(), message);
            mqStepNs = System.nanoTime() - mqStartNs;

            return PurchaseTicketResponse.builder()
                    .code(0)
                    .orderId(orderId)
                    .ticketTierId(ticketTierId)
                    .purchaseCount(purchaseCount)
                    .prepayCode(prepayCode)
                    .build();
        } finally {
            long totalNs = System.nanoTime() - purchaseStartNs;
            log.debug("purchase_step=summary userId={} ticketTierId={} orderId={} totalMs={} redisMs={} paymentMs={} mqMs={}",
                    userId,
                    ticketTierId,
                    orderId,
                    totalNs / 1_000_000.0,
                    redisStepNs < 0 ? -1.0 : redisStepNs / 1_000_000.0,
                    paymentStepNs < 0 ? -1.0 : paymentStepNs / 1_000_000.0,
                    mqStepNs < 0 ? -1.0 : mqStepNs / 1_000_000.0);
        }
    }

    private long executePurchaseScript(String inventoryKey,
                                       String userOrderedKey,
                                       String ticketInfoKey,
                                       int purchaseCount,
                                       long nowEpochSecond) {
        byte[] sha = purchaseLuaSha1.getBytes(StandardCharsets.UTF_8);

        try {
            return executeBySha(sha, inventoryKey, userOrderedKey, ticketInfoKey, purchaseCount, nowEpochSecond);
        } catch (RedisSystemException ex) {
            // Reload script and retry once when Redis loses script cache.
            if (ex.getMessage() != null && ex.getMessage().contains("NOSCRIPT")) {
                this.purchaseLuaSha1 = loadScriptToRedis(this.purchaseLuaScript.getBytes(StandardCharsets.UTF_8));
                return executeBySha(this.purchaseLuaSha1.getBytes(StandardCharsets.UTF_8), inventoryKey, userOrderedKey,
                        ticketInfoKey, purchaseCount, nowEpochSecond);
            }
            throw ex;
        }
    }

    private long executeBySha(byte[] sha,
                              String inventoryKey,
                              String userOrderedKey,
                              String ticketInfoKey,
                              int purchaseCount,
                              long nowEpochSecond) {
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            Object evalResult = connection.scriptingCommands().evalSha(
                    sha,
                    ReturnType.INTEGER,
                    3,
                    inventoryKey.getBytes(StandardCharsets.UTF_8),
                    userOrderedKey.getBytes(StandardCharsets.UTF_8),
                    ticketInfoKey.getBytes(StandardCharsets.UTF_8),
                    Integer.toString(purchaseCount).getBytes(StandardCharsets.UTF_8),
                    Long.toString(nowEpochSecond).getBytes(StandardCharsets.UTF_8));
            return evalResult == null ? 99L : ((Number) evalResult).longValue();
        });
        return result == null ? 99L : result;
    }

    private String readLuaScript() {
        ClassPathResource luaResource = new ClassPathResource("lua/purchase_ticket.lua");
        try (InputStream inputStream = luaResource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read lua script: lua/purchase_ticket.lua", ex);
        }
    }

    private String loadScriptToRedis(byte[] scriptBytes) {
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(
                redisTemplate.getConnectionFactory(),
                "RedisConnectionFactory must not be null");
        try (RedisConnection connection = connectionFactory.getConnection()) {
            return connection.scriptingCommands().scriptLoad(scriptBytes);
        }
    }

}
