package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;
import lombok.Data;

/**
 * Domain event published when ticket is used
 */
@Data
public class OrderUsedEvent extends OrderDomainEvent {

    private String verifyCode;

    public OrderUsedEvent() {
    }

    public OrderUsedEvent(String orderId, String verifyCode) {
        super(orderId, OrderEvent.USE);
        this.verifyCode = verifyCode;
    }

}
