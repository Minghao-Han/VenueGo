package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Command to cancel an order
 */
@Data
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

}

