package com.ticketing.order.domain.order.enums;

import lombok.Getter;

/**
 * Order status enumeration
 * Defines all valid states in the order lifecycle
 */
@Getter
public enum OrderStatus {
    /**
     * Waiting for payment
     */
    PENDING_PAY("pending_pay"),
    /**
     * Payment successfully received
     */
    PAID("paid"),
    /**
     * Order timeout - payment not received within timeout window
     */
    TIMEOUT_CANCELLED("timeout_cancelled"),
    /**
     * User ticket consumed
     */
    USED("used"),
    /**
     * Order cancelled by user or system
     */
    CANCELLED("cancelled"),
    /**
     * Refund process initiated due to late payment after status change
     */
    REFUNDING("refunding");

    private final String code;

    OrderStatus(String code) {
        this.code = code;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
