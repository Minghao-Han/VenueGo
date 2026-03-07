package com.ticketing.order.domain.statemachine.condition;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderStatus;
import org.springframework.stereotype.Component;

/**
 * Guard condition: Allow PAY event only when order is in PENDING_PAY or TIMEOUT_CANCELLED or CANCELLED state
 */
@Component
public class PayCondition implements OrderCondition {

    @Override
    public boolean evaluate(OrderAggregate aggregate) {
        OrderStatus status = aggregate.getStatus();
        return OrderStatus.PENDING_PAY.equals(status)
                || OrderStatus.TIMEOUT_CANCELLED.equals(status)
                || OrderStatus.CANCELLED.equals(status);
    }
}
