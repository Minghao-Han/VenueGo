package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to cancel an order
 */
public class CancelOrderCmd {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private String reason = "User initiated cancellation";

    public CancelOrderCmd() {
    }

    public CancelOrderCmd(String orderId) {
        this.orderId = orderId;
    }

    public CancelOrderCmd(String orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

