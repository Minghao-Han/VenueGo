package com.ticketing.order.domain.statemachine.condition;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderStatus;
import org.springframework.stereotype.Component;

/**
 * Guard condition: Allow REFUND_DONE event when order is in TIMEOUT_CANCELLED or CANCELLED or PAID state
 * The PAID state is included to handle refund requests from paid orders
 */
@Component
public class RefundCondition implements OrderCondition {

    @Override
    public boolean evaluate(OrderAggregate aggregate) {
        OrderStatus status = aggregate.getStatus();
        return OrderStatus.TIMEOUT_CANCELLED.equals(status)
                || OrderStatus.CANCELLED.equals(status)
                || OrderStatus.PAID.equals(status);
    }
}
