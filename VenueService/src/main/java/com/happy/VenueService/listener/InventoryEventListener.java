package com.happy.VenueService.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.happy.VenueService.event.TicketTiersDeletedEvent;
import com.happy.VenueService.service.InventorySyncClient;

@Component
public class InventoryEventListener {

    private final InventorySyncClient inventorySyncClient;

    public InventoryEventListener(InventorySyncClient inventorySyncClient) {
        this.inventorySyncClient = inventorySyncClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTiersDeleted(TicketTiersDeletedEvent event) {
        inventorySyncClient.deleteInventories(event.tierIds());
    }
}
