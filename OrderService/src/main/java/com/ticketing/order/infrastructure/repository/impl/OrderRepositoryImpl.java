package com.ticketing.order.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderStatus;
import com.ticketing.order.infrastructure.repository.OrderRepository;
import com.ticketing.order.infrastructure.repository.entity.OrderEntity;
import com.ticketing.order.infrastructure.repository.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of OrderRepository using MyBatis Plus
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderRepositoryImpl.class);

    private final OrderMapper orderMapper;

    public OrderRepositoryImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public void save(OrderAggregate aggregate) {
        OrderEntity entity = aggregateToEntity(aggregate);
        entity.setId(aggregate.getOrderId());
        entity.setVersion(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        int rows = this.orderMapper.insert(entity);
        if (rows != 1) {
            throw new RuntimeException("Failed to save order: " + aggregate.getOrderId());
        }

        log.debug("Order saved with ID: {}", aggregate.getOrderId());
    }

    @Override
    public Optional<OrderAggregate> findById(String orderId) {
        OrderEntity entity = this.orderMapper.selectOne(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getId, orderId));

        if (entity == null) {
            return Optional.empty();
        }

        OrderAggregate aggregate = entityToAggregate(entity);
        return Optional.of(aggregate);
    }

    @Override
    public boolean updateWithOptimisticLock(OrderAggregate aggregate, Integer expectedVersion) {
        OrderEntity entity = aggregateToEntity(aggregate);
        entity.setId(aggregate.getOrderId());
        entity.setUpdatedAt(LocalDateTime.now());

        // Execute optimistic lock update
        // UPDATE orders SET status = ?, version = version + 1, ... 
        // WHERE id = ? AND version = ? AND status = ?
        int affectedRows = this.orderMapper.update(
                entity,
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getId, aggregate.getOrderId())
                        .eq(OrderEntity::getVersion, expectedVersion));

        if (affectedRows == 1) {
            log.debug("Order updated successfully with optimistic lock: {}", aggregate.getOrderId());
            return true;
        } else if (affectedRows == 0) {
            log.warn("Optimistic lock conflict for order: {}", aggregate.getOrderId());
            return false;
        } else {
            throw new RuntimeException(
                    "Unexpected affected rows: " + affectedRows
                            + " for order: " + aggregate.getOrderId());
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
                .version(aggregate.getVersion() != null ? aggregate.getVersion() + 1 : 1)
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
