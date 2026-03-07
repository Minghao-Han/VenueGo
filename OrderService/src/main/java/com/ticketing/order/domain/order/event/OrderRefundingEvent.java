package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;
import lombok.Data;

/**
 * Domain event published when refund is initiated
 */
@Data
public class OrderRefundingEvent extends OrderDomainEvent {

    private String reason;

    public OrderRefundingEvent() {
    }

    public OrderRefundingEvent(String orderId, String reason) {
        super(orderId, OrderEvent.REFUND_DONE);
        this.reason = reason;
    }

}
