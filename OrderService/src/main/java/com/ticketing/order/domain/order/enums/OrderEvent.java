package com.ticketing.order.domain.order.enums;

/**
 * Order event enumeration
 * Defines all events that can trigger state transitions
 */
public enum OrderEvent {
    /**
     * Payment successful event
     */
    PAY("pay"),
    /**
     * Payment timeout event
     */
    TIMEOUT("timeout"),
    /**
     * User-initiated cancellation event
     */
    CANCEL("cancel"),
    /**
     * Ticket usage event
     */
    USE("use"),
    /**
     * Refund completion event
     */
    REFUND_DONE("refund_done");

    private final String code;

    OrderEvent(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OrderEvent fromCode(String code) {
        for (OrderEvent event : OrderEvent.values()) {
            if (event.code.equals(code)) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown event code: " + code);
    }
}
