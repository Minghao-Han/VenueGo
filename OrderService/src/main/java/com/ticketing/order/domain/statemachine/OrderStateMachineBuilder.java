package com.ticketing.order.domain.statemachine;

import com.ticketing.order.common.exception.OrderStateException;
import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderEvent;
import com.ticketing.order.domain.order.enums.OrderStatus;
import com.ticketing.order.domain.statemachine.action.*;
import com.ticketing.order.domain.statemachine.condition.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Order State Machine Implementation
 * 
 * This is the single source of truth for all valid state transitions in the order lifecycle.
 * All transitions must be defined here. Adding a new transition means adding a new entry here.
 * 
 * The state machine is stateless and shared across all orders.
 * It is built once at startup and reused.
 * The current order state is always loaded from DB before firing an event.
 */
@Component
public class OrderStateMachineBuilder {

    private static final Logger log = LoggerFactory.getLogger(OrderStateMachineBuilder.class);

    /**
     * Transition definition: maps (sourceState, event) -> (targetState, condition, action)
     */
    private static class TransitionDef {
        final OrderStatus targetState;
        final OrderCondition condition;
        final OrderAction action;

        TransitionDef(OrderStatus targetState, OrderCondition condition, OrderAction action) {
            this.targetState = targetState;
            this.condition = condition;
            this.action = action;
        }
    }

    /**
     * Transition table: (sourceState, event) -> TransitionDef
     */
    private final Map<String, TransitionDef> transitionTable = new HashMap<>();

    public OrderStateMachineBuilder(
            PayCondition payCondition,
            TimeoutCondition timeoutCondition,
            CancelCondition cancelCondition,
            UseCondition useCondition,
            RefundCondition refundCondition,
            PayAction payAction,
            TimeoutAction timeoutAction,
            CancelAction cancelAction,
            UseAction useAction,
            RefundAction refundAction) {

        // Define all valid state transitions
        defineTransitions(
                payCondition, timeoutCondition, cancelCondition, useCondition, refundCondition,
                payAction, timeoutAction, cancelAction, useAction, refundAction);

        log.info("Order state machine initialized with {} transitions", transitionTable.size());
    }

    private void defineTransitions(
            PayCondition payCondition,
            TimeoutCondition timeoutCondition,
            CancelCondition cancelCondition,
            UseCondition useCondition,
            RefundCondition refundCondition,
            PayAction payAction,
            TimeoutAction timeoutAction,
            CancelAction cancelAction,
            UseAction useAction,
            RefundAction refundAction) {

        // Transition 1: PENDING_PAY + PAY -> PAID
        addTransition(OrderStatus.PENDING_PAY, OrderEvent.PAY, OrderStatus.PAID, payCondition, payAction);

        // Transition 2: PENDING_PAY + TIMEOUT -> TIMEOUT_CANCELLED
        addTransition(OrderStatus.PENDING_PAY, OrderEvent.TIMEOUT, OrderStatus.TIMEOUT_CANCELLED, timeoutCondition, timeoutAction);

        // Transition 3: PENDING_PAY + CANCEL -> CANCELLED
        addTransition(OrderStatus.PENDING_PAY, OrderEvent.CANCEL, OrderStatus.CANCELLED, cancelCondition, cancelAction);

        // Transition 4: PAID + USE -> USED
        addTransition(OrderStatus.PAID, OrderEvent.USE, OrderStatus.USED, useCondition, useAction);

        // Transition 5: PAID + REFUND_DONE -> CANCELLED
        addTransition(OrderStatus.PAID, OrderEvent.REFUND_DONE, OrderStatus.CANCELLED, refundCondition, refundAction);

        // Transition 6: TIMEOUT_CANCELLED + PAY -> REFUNDING
        // Critical: handles late payment callback after timeout
        addTransition(OrderStatus.TIMEOUT_CANCELLED, OrderEvent.PAY, OrderStatus.REFUNDING, payCondition, refundAction);

        // Transition 7: CANCELLED + PAY -> REFUNDING
        // Critical: handles late payment callback after user cancellation
        addTransition(OrderStatus.CANCELLED, OrderEvent.PAY, OrderStatus.REFUNDING, payCondition, refundAction);
    }

    private void addTransition(
            OrderStatus sourceState,
            OrderEvent event,
            OrderStatus targetState,
            OrderCondition condition,
            OrderAction action) {

        String key = sourceState + "|" + event;
        transitionTable.put(key, new TransitionDef(targetState, condition, action));
        log.debug("Registered transition: {} + {} -> {}", sourceState, event, targetState);
    }

    /**
     * Fire an event on the state machine
     * 
     * @param aggregate The order aggregate
     * @param event The event to fire
     * @param context The context containing event parameters
     * @return true if transition was successful, false if condition rejected
     * @throws OrderStateException if no transition exists for current state + event
     */
    public boolean fireEvent(
            OrderAggregate aggregate,
            OrderEvent event,
            OrderAction.ActionContext context) {

        OrderStatus currentState = aggregate.getStatus();
        String key = currentState + "|" + event;

        TransitionDef transition = transitionTable.get(key);
        if (transition == null) {
            log.warn("No transition defined: {} + {}", currentState, event);
            throw new OrderStateException(
                    "No valid transition for state: " + currentState + " with event: " + event);
        }

        // Check condition
        if (!transition.condition.evaluate(aggregate)) {
            log.warn("Condition rejected transition: {} + {}", currentState, event);
            return false;
        }

        // Execute action (which updates aggregate and enqueues domain events)
        try {
            transition.action.execute(aggregate, context);
            log.debug("State transition executed: {} + {} -> {}",
                    currentState,
                    event,
                    transition.targetState);
            return true;
        } catch (Exception e) {
            log.error("Error executing action for transition: {} + {}", currentState, event, e);
            throw new OrderStateException("Error executing state transition", e);
        }
    }

    /**
     * Get the target state for a given source state and event
     */
    public Optional<OrderStatus> getTargetState(OrderStatus sourceState, OrderEvent event) {
        String key = sourceState + "|" + event;
        TransitionDef transition = transitionTable.get(key);
        return transition != null ? Optional.of(transition.targetState) : Optional.empty();
    }

    /**
     * Check if a transition is valid for the given aggregate state and event
     */
    public boolean isTransitionValid(OrderAggregate aggregate, OrderEvent event) {
        String key = aggregate.getStatus() + "|" + event;
        TransitionDef transition = transitionTable.get(key);
        return transition != null && transition.condition.evaluate(aggregate);
    }

    /**
     * Get all valid events for a given state
     */
    public Set<OrderEvent> getValidEventsForState(OrderStatus state) {
        Set<OrderEvent> validEvents = new HashSet<>();
        for (String key : transitionTable.keySet()) {
            String[] parts = key.split("\\|");
            if (parts.length == 2 && parts[0].equals(state.name())) {
                validEvents.add(OrderEvent.valueOf(parts[1]));
            }
        }
        return validEvents;
    }
}
