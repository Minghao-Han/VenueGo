package com.happy.TicketingService.service;

import java.util.UUID;

import com.happy.TicketingService.dto.PurchaseTicketResponse;

public interface TicketPurchaseService {

    PurchaseTicketResponse purchase(UUID userId, UUID ticketTierId, Integer purchaseCount);
}
