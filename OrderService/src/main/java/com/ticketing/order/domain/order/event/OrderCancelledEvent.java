package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;
import lombok.Data;

/**
 * Domain event published when order is cancelled
 */
@Data
public class OrderCancelledEvent extends OrderDomainEvent {

    private String reason;

    public OrderCancelledEvent() {
    }

    public OrderCancelledEvent(String orderId, String reason) {
        super(orderId, OrderEvent.CANCEL);
        this.reason = reason;
    }

}
