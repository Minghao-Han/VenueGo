package com.happy.TicketingService.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.happy.TicketingService.service.PaymentService;

@Service
public class MockPaymentService implements PaymentService {

    @Override
    public String generatePrepayCode(UUID userId, UUID ticketTierId, UUID orderId, Integer purchaseCount) {
        return "PREPAY-" + userId + "-" + ticketTierId + "-" + orderId + "-" + purchaseCount;
    }
}
