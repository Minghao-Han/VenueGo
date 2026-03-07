package com.ticketing.order.domain.statemachine.action;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import org.springframework.stereotype.Component;

/**
 * Action for CANCEL event: calls aggregate.cancel()
 */
@Component
public class CancelAction implements OrderAction {

    @Override
    public void execute(OrderAggregate aggregate, ActionContext context) {
        // Call domain command handler
        aggregate.cancel(context.getReason());

        // No side effects for cancel - just state transition
    }
}
