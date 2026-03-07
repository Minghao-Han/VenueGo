package com.ticketing.order.infrastructure.payment.impl;

import com.ticketing.order.infrastructure.payment.PaymentQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of PaymentQueryService for development and testing
 * Always returns UNPAID for any payment query
 */
@Component("mockPaymentQueryService")
@Primary
public class MockPaymentQueryService implements PaymentQueryService {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentQueryService.class);

    @Override
    public PaymentStatus queryPaymentStatus(String paymentId) throws PaymentQueryException {
        log.info("Mock: querying payment status for payment ID: {}", paymentId);
        // For development: can be configured to return different statuses
        return PaymentStatus.UNPAID;
    }
}
