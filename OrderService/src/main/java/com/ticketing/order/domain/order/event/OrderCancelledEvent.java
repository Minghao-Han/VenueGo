package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;

/**
 * Domain event published when order is cancelled
 */
public class OrderCancelledEvent extends OrderDomainEvent {

    private String reason;

    public OrderCancelledEvent() {
    }

    public OrderCancelledEvent(String orderId, String reason) {
        super(orderId, OrderEvent.CANCEL);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
