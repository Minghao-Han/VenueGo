package com.ticketing.order.infrastructure.repository;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;

import java.util.Optional;

/**
 * Repository interface for Order aggregate
 * Abstracts data persistence
 */
public interface OrderRepository {

    /**
     * Save a new order to database
     */
    void save(OrderAggregate aggregate);

    /**
     * Load an order from database by ID
     */
    Optional<OrderAggregate> findById(String orderId);

    /**
     * Update order with optimistic lock
     * 
     * @param aggregate the order aggregate with new state
     * @param expectedVersion the version expected in DB (read at the start)
     * @return true if update succeeded (affected rows = 1), false if version conflict
     */
    boolean updateWithOptimisticLock(OrderAggregate aggregate, Integer expectedVersion);
}
