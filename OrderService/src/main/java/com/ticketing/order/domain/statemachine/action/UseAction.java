package com.ticketing.order.domain.statemachine.action;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import org.springframework.stereotype.Component;

/**
 * Action for USE event: calls aggregate.use()
 */
@Component
public class UseAction implements OrderAction {

    @Override
    public void execute(OrderAggregate aggregate, ActionContext context) {
        // Call domain command handler
        aggregate.use(context.getVerifyCode());

        // Record side effects: mark ticket as consumed in ticket service
        aggregate.enqueueSideEffect(() -> {
            // Ticket service update would be called here
        });
    }
}
