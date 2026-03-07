package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Command to use/consume a ticket
 */
@Data
public class UseTicketCmd {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Verify code is required")
    private String verifyCode;

    public UseTicketCmd() {
    }

    public UseTicketCmd(String orderId, String verifyCode) {
        this.orderId = orderId;
        this.verifyCode = verifyCode;
    }

}

