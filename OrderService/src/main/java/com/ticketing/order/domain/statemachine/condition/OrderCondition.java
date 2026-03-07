package com.ticketing.order.domain.statemachine.condition;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;

/**
 * Base interface for all order state machine conditions
 * Condition only answers: is the current state a legal source state for this event?
 * Condition does NOT contain business rule logic - that belongs to the action
 */
@FunctionalInterface
public interface OrderCondition {

    /**
     * Evaluate if the transition is allowed
     * 
     * @param aggregate The order aggregate with current state
     * @return true if transition is allowed, false otherwise
     */
    boolean evaluate(OrderAggregate aggregate);
}
