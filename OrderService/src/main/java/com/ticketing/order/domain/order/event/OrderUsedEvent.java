package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;

/**
 * Domain event published when ticket is used
 */
public class OrderUsedEvent extends OrderDomainEvent {

    private String verifyCode;

    public OrderUsedEvent() {
    }

    public OrderUsedEvent(String orderId, String verifyCode) {
        super(orderId, OrderEvent.USE);
        this.verifyCode = verifyCode;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
