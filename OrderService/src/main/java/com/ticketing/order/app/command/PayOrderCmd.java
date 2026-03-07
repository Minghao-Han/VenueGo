package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Command to pay for an order
 */
@Data
public class PayOrderCmd {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    public PayOrderCmd() {
    }

    public PayOrderCmd(String orderId, String paymentId, BigDecimal amount) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
    }

}

