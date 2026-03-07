package com.ticketing.order.domain.order.event;

import com.ticketing.order.domain.order.enums.OrderEvent;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Domain event published when order is successfully paid
 */
@Data
public class OrderPaidEvent extends OrderDomainEvent {

    private String paymentId;
    private BigDecimal amount;

    public OrderPaidEvent() {
    }

    public OrderPaidEvent(String orderId, String paymentId, BigDecimal amount) {
        super(orderId, OrderEvent.PAY);
        this.paymentId = paymentId;
        this.amount = amount;
    }

}
