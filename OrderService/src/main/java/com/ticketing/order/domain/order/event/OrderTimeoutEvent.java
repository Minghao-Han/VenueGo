package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;
import lombok.Data;

/**
 * Domain event published when payment timeout occurs
 */
@Data
public class OrderTimeoutEvent extends OrderDomainEvent {

    private String reason;

    public OrderTimeoutEvent() {
    }

    public OrderTimeoutEvent(String orderId, String reason) {
        super(orderId, OrderEvent.TIMEOUT);
        this.reason = reason;
    }

}
