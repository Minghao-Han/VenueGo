package com.happy.TicketingService.service.impl;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.happy.TicketingService.common.TicketRedisKeyBuilder;
import com.happy.TicketingService.entity.TicketInventory;
import com.happy.TicketingService.event.TicketPurchaseEvent;
import com.happy.TicketingService.repository.TicketInventoryRepository;
import com.happy.TicketingService.service.InventoryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    private final TicketInventoryRepository ticketInventoryRepository;
    private final StringRedisTemplate redisTemplate;
    private final TicketRedisKeyBuilder ticketRedisKeyBuilder;

    public InventoryServiceImpl(TicketInventoryRepository ticketInventoryRepository,
                                StringRedisTemplate redisTemplate,
                                TicketRedisKeyBuilder ticketRedisKeyBuilder) {
        this.ticketInventoryRepository = ticketInventoryRepository;
        this.redisTemplate = redisTemplate;
        this.ticketRedisKeyBuilder = ticketRedisKeyBuilder;
    }

    @Override
    @Transactional
    public void decreaseInventoryByPurchase(TicketPurchaseEvent event) {
        int updated = ticketInventoryRepository.decreaseAvailableInventoryIfEnoughByTicketTierId(
                event.getTicketTierId(),
                event.getPurchaseCount());

        if (updated == 0) {
            log.warn("Inventory DB deduction failed by SQL guard, ticketTierId={}, orderId={}, messageId={}",
                    event.getTicketTierId(), event.getOrderId(), event.getMessageId());
        }
    }

    @Override
    public void syncRedisCache(TicketInventory inventory) {
        if (inventory == null
                || inventory.getTicketTierId() == null
                || inventory.getAvailableInventory() == null
                || inventory.getPurchaseLimit() == null
                || inventory.getSaleStartTime() == null
                || inventory.getSaleEndTime() == null) {
            return;
        }

        long ttlSeconds = Duration.between(OffsetDateTime.now(ZoneOffset.UTC), inventory.getSaleEndTime()).toSeconds();
        if (ttlSeconds <= 0) {
            return;
        }

        String inventoryKey = ticketRedisKeyBuilder.buildInventoryKey(inventory.getTicketTierId());
        String infoKey = ticketRedisKeyBuilder.buildInfoKey(inventory.getTicketTierId());

        redisTemplate.opsForValue().set(
                inventoryKey,
                String.valueOf(inventory.getAvailableInventory()),
                Duration.ofSeconds(ttlSeconds));
        redisTemplate.opsForHash().put(infoKey, "saleStartEpoch", String.valueOf(inventory.getSaleStartTime().toEpochSecond()));
        redisTemplate.opsForHash().put(infoKey, "saleEndEpoch", String.valueOf(inventory.getSaleEndTime().toEpochSecond()));
        redisTemplate.opsForHash().put(infoKey, "purchaseLimit", String.valueOf(inventory.getPurchaseLimit()));
        redisTemplate.expire(infoKey, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    @Transactional
    public TicketInventory upsert(UUID ticketTierId,
                                  Integer availableInventory,
                                  Integer purchaseLimit,
                                  OffsetDateTime saleStartTime,
                                  OffsetDateTime saleEndTime) {
        TicketInventory inventory = ticketInventoryRepository.findByTicketTierId(ticketTierId)
                .orElseGet(TicketInventory::new);

        inventory.setTicketTierId(ticketTierId);
        inventory.setAvailableInventory(availableInventory);
        inventory.setPurchaseLimit(purchaseLimit);
        inventory.setSaleStartTime(saleStartTime);
        inventory.setSaleEndTime(saleEndTime);

        TicketInventory saved = ticketInventoryRepository.save(inventory);
        syncRedisCache(saved);
        return saved;
    }

    @Override
    @Transactional
    public int deleteInventories(List<UUID> ticketTierIds) {
        if (ticketTierIds == null || ticketTierIds.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;
        for (UUID ticketTierId : ticketTierIds) {
            if (ticketTierId == null) {
                continue;
            }

            deletedCount += ticketInventoryRepository.deleteByTicketTierId(ticketTierId);
            redisTemplate.delete(ticketRedisKeyBuilder.buildInventoryKey(ticketTierId));
            redisTemplate.delete(ticketRedisKeyBuilder.buildInfoKey(ticketTierId));
        }

        return deletedCount;
    }

    @Override
    public void prewarmInventoryCache() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<TicketInventory> activeInventories = ticketInventoryRepository.findBySaleEndTimeAfter(now);

        if (activeInventories.isEmpty()) {
            log.info("No active inventories found, skip prewarm");
            return;
        }

        TicketInventory probeInventory = activeInventories.get(0);
        String probeInventoryKey = ticketRedisKeyBuilder.buildInventoryKey(probeInventory.getTicketTierId());
        String probeInfoKey = ticketRedisKeyBuilder.buildInfoKey(probeInventory.getTicketTierId());
        Boolean inventoryKeyExists = redisTemplate.hasKey(probeInventoryKey);
        Boolean infoKeyExists = redisTemplate.hasKey(probeInfoKey);
        if (Boolean.TRUE.equals(inventoryKeyExists) && Boolean.TRUE.equals(infoKeyExists)) {
            log.info("Skip prewarm because cache probe hit, ticketTierId={}", probeInventory.getTicketTierId());
            return;
        }

        for (TicketInventory inventory : activeInventories) {
            try {
                syncRedisCache(inventory);
            } catch (Exception ex) {
                log.warn("Failed to prewarm inventory cache for ticketTierId={}", inventory.getTicketTierId(), ex);
            }
        }

        log.info("Prewarmed {} ticket inventories into Redis", activeInventories.size());
    }
}
