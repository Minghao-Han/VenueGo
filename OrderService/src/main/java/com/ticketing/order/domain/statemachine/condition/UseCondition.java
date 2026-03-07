package com.ticketing.order.domain.statemachine.condition;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderStatus;
import org.springframework.stereotype.Component;

/**
 * Guard condition: Allow USE event only when order is in PAID state
 */
@Component
public class UseCondition implements OrderCondition {

    @Override
    public boolean evaluate(OrderAggregate aggregate) {
        return OrderStatus.PAID.equals(aggregate.getStatus());
    }
}
