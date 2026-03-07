package com.ticketing.order.domain.statemachine.action;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import org.springframework.stereotype.Component;

/**
 * Action for TIMEOUT event: calls aggregate.timeout()
 */
@Component
public class TimeoutAction implements OrderAction {

    @Override
    public void execute(OrderAggregate aggregate, ActionContext context) {
        // Call domain command handler
        aggregate.timeout(context.getReason());

        // No side effects for timeout - just state transition
    }
}
