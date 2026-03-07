package com.ticketing.order.domain.statemachine.action;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import org.springframework.stereotype.Component;

/**
 * Action for PAY event: calls aggregate.pay() and records side effects
 */
@Component
public class PayAction implements OrderAction {

    @Override
    public void execute(OrderAggregate aggregate, ActionContext context) {
        // Call domain command handler
        aggregate.pay(context.getPaymentId(), context.getAmount());

        // Record side effects to be executed after optimistic lock succeeds
        // Example: deduct inventory, generate ticket
        aggregate.enqueueSideEffect(() -> {
            // Inventory deduction would be called here
            // Ticket generation would be called here
        });
    }
}
