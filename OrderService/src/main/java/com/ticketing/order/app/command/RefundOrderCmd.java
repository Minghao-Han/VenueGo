package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to refund an order
 */
public class RefundOrderCmd {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private String reason = "Refund request";

    public RefundOrderCmd() {
    }

    public RefundOrderCmd(String orderId) {
        this.orderId = orderId;
    }

    public RefundOrderCmd(String orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

