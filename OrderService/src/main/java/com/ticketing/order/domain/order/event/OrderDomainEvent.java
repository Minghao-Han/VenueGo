package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;

import java.time.LocalDateTime;

/**
 * Base class for all domain events in order aggregate
 */
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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderEvent getEventType() {
        return eventType;
    }

    public void setEventType(OrderEvent eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
