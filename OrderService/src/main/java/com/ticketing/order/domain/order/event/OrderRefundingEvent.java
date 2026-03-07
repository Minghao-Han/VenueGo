package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;

/**
 * Domain event published when refund is initiated
 */
public class OrderRefundingEvent extends OrderDomainEvent {

    private String reason;

    public OrderRefundingEvent() {
    }

    public OrderRefundingEvent(String orderId, String reason) {
        super(orderId, OrderEvent.REFUND_DONE);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
