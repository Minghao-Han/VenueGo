package com.happy.TicketingService.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.happy.TicketingService.entity.TicketInventory;
import com.happy.TicketingService.event.TicketPurchaseEvent;

public interface InventoryService {

    void decreaseInventoryByPurchase(TicketPurchaseEvent event);

    void syncRedisCache(TicketInventory inventory);

    TicketInventory upsert(UUID ticketTierId,
                           Integer availableInventory,
                           Integer purchaseLimit,
                           OffsetDateTime saleStartTime,
                           OffsetDateTime saleEndTime);

    int deleteInventories(List<UUID> ticketTierIds);

    void prewarmInventoryCache();
}
