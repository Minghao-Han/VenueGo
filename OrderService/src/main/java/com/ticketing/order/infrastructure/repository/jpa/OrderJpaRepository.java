package com.ticketing.order.infrastructure.repository.jpa;

import com.ticketing.order.infrastructure.repository.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for order persistence.
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {
}
