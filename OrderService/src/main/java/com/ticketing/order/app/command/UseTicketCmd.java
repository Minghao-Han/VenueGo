package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to use/consume a ticket
 */
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

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getVerifyCode() { return verifyCode; }
    public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }
}

