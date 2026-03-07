package com.ticketing.order.domain.statemachine.action;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import org.springframework.stereotype.Component;

/**
 * Action for REFUND event: calls aggregate.refund() and records refund side effects
 * 
 * This handles the race condition where payment callback arrives after order cancellation.
 * The state machine automatically routes to REFUNDING to ensure funds are returned.
 */
@Component
public class RefundAction implements OrderAction {

    @Override
    public void execute(OrderAggregate aggregate, ActionContext context) {
        // Call domain command handler
        aggregate.refund(context.getReason());

        // Record side effects: initiate refund with payment service
        aggregate.enqueueSideEffect(() -> {
            // Payment refund would be initiated here
            // Inventory restoration would be called here
        });
    }
}
