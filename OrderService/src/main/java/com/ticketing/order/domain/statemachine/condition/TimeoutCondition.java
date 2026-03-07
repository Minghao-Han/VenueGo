package com.ticketing.order.domain.statemachine.condition;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderStatus;
import org.springframework.stereotype.Component;

/**
 * Guard condition: Allow TIMEOUT event only when order is in PENDING_PAY state
 */
@Component
public class TimeoutCondition implements OrderCondition {

    @Override
    public boolean evaluate(OrderAggregate aggregate) {
        return OrderStatus.PENDING_PAY.equals(aggregate.getStatus());
    }
}
