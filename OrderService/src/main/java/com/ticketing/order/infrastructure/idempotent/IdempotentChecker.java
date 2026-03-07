package com.ticketing.order.infrastructure.idempotent;

/**
 * Interface for idempotency checking
 * Implementations use Redis to store processed messages/events
 */
public interface IdempotentChecker {

    /**
     * Check if a request/message has been processed before
     * 
     * @param idempotentKey the unique key for the request/message
     * @return true if already processed, false otherwise
     */
    boolean isProcessed(String idempotentKey);

    /**
     * Mark a request/message as processed
     * 
     * @param idempotentKey the unique key for the request/message
     */
    void markAsProcessed(String idempotentKey);

    /**
     * Check and mark in one atomic operation
     * 
     * @param idempotentKey the unique key for the request/message
     * @return true if this is the first time (not processed before), false if already processed
     */
    boolean checkAndMarkAsProcessed(String idempotentKey);
}
