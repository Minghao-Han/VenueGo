package com.happy.TicketingService.service;

import java.util.UUID;

public interface PaymentService {

    String generatePrepayCode(UUID userId, UUID ticketTierId, UUID orderId, Integer purchaseCount);
}
