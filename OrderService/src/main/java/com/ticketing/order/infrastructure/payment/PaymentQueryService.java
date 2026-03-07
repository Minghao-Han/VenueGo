package com.ticketing.order.infrastructure.payment;

/**
 * Interface for querying payment status from payment platform
 * Implementations switch payment providers via dependency injection
 */
public interface PaymentQueryService {

    /**
     * Query payment status from platform
     * 
     * @param paymentId the payment ID to query
     * @return payment status (PAID, UNPAID, or null if error)
     * @throws PaymentQueryException if query fails
     */
    PaymentStatus queryPaymentStatus(String paymentId) throws PaymentQueryException;

    /**
     * Payment status enumeration
     */
    enum PaymentStatus {
        PAID,
        UNPAID
    }

    /**
     * Exception thrown when payment query fails
     */
    class PaymentQueryException extends RuntimeException {
        public PaymentQueryException(String message) {
            super(message);
        }

        public PaymentQueryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
