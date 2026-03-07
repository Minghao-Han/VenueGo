package com.ticketing.order.common.exception;

import lombok.Getter;

/**
 * Exception thrown when the state machine condition rejects a transition
 * This indicates an illegal state for the given event
 */
public class OrderStateException extends RuntimeException {

    @Getter
    private String code;

    public OrderStateException(String message) {
        super(message);
        this.code = "ORDER_STATE_ERROR";
    }

    public OrderStateException(String code, String message) {
        super(message);
        this.code = code;
    }

    public OrderStateException(String message, Throwable cause) {
        super(message, cause);
        this.code = "ORDER_STATE_ERROR";
    }

}
