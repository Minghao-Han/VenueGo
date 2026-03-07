package com.ticketing.order.domain.statemachine.action;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import lombok.Data;

/**
 * Base interface for all order state machine actions
 * Action is responsible for:
 * - Calling the corresponding aggregate method
 * - Recording side effects into the aggregate's pending-side-effects list
 * 
 * Action does NOT directly call external services or write to DB.
 * All side effects are executed after the optimistic lock write succeeds.
 */
@FunctionalInterface
public interface OrderAction {

    /**
     * Execute the state transition action
     * 
     * @param aggregate The order aggregate to modify
     * @param context   The action context containing required parameters
     */
    void execute(OrderAggregate aggregate, ActionContext context);

    /**
     * Action context holding parameters needed for specific actions
     */
    @Data
    class ActionContext {
        private String paymentId;
        private java.math.BigDecimal amount;
        private String reason;
        private String verifyCode;

        public ActionContext() {
        }
    }
}
