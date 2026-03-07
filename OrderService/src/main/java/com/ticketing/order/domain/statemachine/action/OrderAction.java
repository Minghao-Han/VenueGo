package com.ticketing.order.domain.statemachine.action;

import com.ticketing.order.domain.order.aggregate.OrderAggregate;

/**
 * Base interface for all order state machine actions
 * Action is responsible for:
 * - Calling the corresponding aggregate method
 * - Recording side effects into the aggregate's pending-side-effects list
 * 
 * Action does NOT directly call external services or write to DB.
 * All side effects are executed after the optimistic lock write succeeds.
 */
@FunctionalInterface
public interface OrderAction {

    /**
     * Execute the state transition action
     * 
     * @param aggregate The order aggregate to modify
     * @param context   The action context containing required parameters
     */
    void execute(OrderAggregate aggregate, ActionContext context);

    /**
     * Action context holding parameters needed for specific actions
     */
    class ActionContext {
        private String paymentId;
        private java.math.BigDecimal amount;
        private String reason;
        private String verifyCode;

        public ActionContext() {
        }

        public String getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public java.math.BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(java.math.BigDecimal amount) {
            this.amount = amount;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getVerifyCode() {
            return verifyCode;
        }

        public void setVerifyCode(String verifyCode) {
            this.verifyCode = verifyCode;
        }
    }
}
