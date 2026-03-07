package com.ticketing.order.infrastructure.repository.impl;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderStatus;
import com.ticketing.order.infrastructure.repository.OrderRepository;
import com.ticketing.order.infrastructure.repository.entity.OrderEntity;
import com.ticketing.order.infrastructure.repository.jpa.OrderJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Default implementation of OrderRepository using Spring Data JPA (Hibernate)
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderRepositoryImpl.class);

    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    @Transactional
    public void save(OrderAggregate aggregate) {
        OrderEntity entity = aggregateToEntity(aggregate);
        entity.setId(aggregate.getOrderId());
        entity.setVersion(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        this.orderJpaRepository.save(entity);

        log.debug("Order saved with ID: {}", aggregate.getOrderId());
    }

    @Override
    public Optional<OrderAggregate> findById(String orderId) {
        return this.orderJpaRepository.findById(orderId)
                .map(this::entityToAggregate);
    }

    @Override
    @Transactional
    public boolean updateWithOptimisticLock(OrderAggregate aggregate, Integer expectedVersion) {
        Optional<OrderEntity> existingOpt = this.orderJpaRepository.findById(aggregate.getOrderId());
        if (existingOpt.isEmpty()) {
            return false;
        }

        OrderEntity existing = existingOpt.get();
        if (!expectedVersion.equals(existing.getVersion())) {
            log.warn("Optimistic lock conflict for order: {}", aggregate.getOrderId());
            return false;
        }

        OrderEntity updated = aggregateToEntity(aggregate);
        updated.setId(existing.getId());
        updated.setVersion(existing.getVersion());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());

        try {
            this.orderJpaRepository.saveAndFlush(updated);
            log.debug("Order updated successfully with optimistic lock: {}", aggregate.getOrderId());
            return true;
        } catch (OptimisticLockingFailureException ex) {
            log.warn("Optimistic lock conflict for order: {}", aggregate.getOrderId());
            return false;
        }
    }

    /**
     * Convert domain aggregate to database entity
     */
    private OrderEntity aggregateToEntity(OrderAggregate aggregate) {
        return OrderEntity.builder()
                .eventId(aggregate.getEventId())
                .venueId(aggregate.getVenueId())
                .ticketTypeId(aggregate.getTicketTypeId())
                .status(aggregate.getStatus().getCode())
                .quantity(aggregate.getQuantity())
                .unitPrice(aggregate.getUnitPrice())
                .totalAmount(aggregate.getTotalAmount())
                .paymentId(aggregate.getPaymentId())
                .verifyCode(aggregate.getVerifyCode())
                .createdAt(aggregate.getCreatedAt())
                .paidAt(aggregate.getPaidAt())
                .usedAt(aggregate.getUsedAt())
                .cancelledAt(aggregate.getCancelledAt())
                .version(aggregate.getVersion())
                .build();
    }

    /**
     * Convert database entity to domain aggregate
     */
    private OrderAggregate entityToAggregate(OrderEntity entity) {
        return OrderAggregate.builder()
                .orderId(entity.getId())
                .eventId(entity.getEventId())
                .venueId(entity.getVenueId())
                .ticketTypeId(entity.getTicketTypeId())
                .status(OrderStatus.fromCode(entity.getStatus()))
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalAmount(entity.getTotalAmount())
                .paymentId(entity.getPaymentId())
                .verifyCode(entity.getVerifyCode())
                .createdAt(entity.getCreatedAt())
                .paidAt(entity.getPaidAt())
                .usedAt(entity.getUsedAt())
                .cancelledAt(entity.getCancelledAt())
                .version(entity.getVersion())
                .build();
    }
}
