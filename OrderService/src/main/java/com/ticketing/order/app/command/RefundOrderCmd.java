package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Command to refund an order
 */
@Data
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

}

