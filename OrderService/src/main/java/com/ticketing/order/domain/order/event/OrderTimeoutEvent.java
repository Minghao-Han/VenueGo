package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;

/**
 * Domain event published when payment timeout occurs
 */
public class OrderTimeoutEvent extends OrderDomainEvent {

    private String reason;

    public OrderTimeoutEvent() {
    }

    public OrderTimeoutEvent(String orderId, String reason) {
        super(orderId, OrderEvent.TIMEOUT);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
