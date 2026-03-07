package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Base class for all domain events in order aggregate
 */
@Data
public abstract class OrderDomainEvent {

    protected String orderId;
    protected OrderEvent eventType;
    protected LocalDateTime occurredAt;

    public OrderDomainEvent() {
    }

    public OrderDomainEvent(String orderId, OrderEvent eventType) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
    }

    public OrderDomainEvent(String orderId, OrderEvent eventType, LocalDateTime occurredAt) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
    }

}
