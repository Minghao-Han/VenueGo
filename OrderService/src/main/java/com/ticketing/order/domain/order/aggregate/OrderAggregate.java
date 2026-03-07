package com.ticketing.order.domain.order.aggregate;

import com.ticketing.order.domain.order.enums.OrderStatus;
import com.ticketing.order.domain.order.event.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order aggregate root - encapsulates all order business logic and state transitions
 * 
 * This is the single source of truth for order state and behavior.
 * Methods in this class are domain command handlers that:
 * - Validate business rules
 * - Handle idempotency
 * - Update aggregate state
 * - Produce domain events
 * - Do NOT access databases, external services, or publish to MQ
 */
@NoArgsConstructor
public class OrderAggregate {

    // Core identity
    private String orderId;
    private String eventId;
    private String venueId;
    private String ticketTypeId;

    // Order state
    private OrderStatus status;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;

    // Payment information
    private String paymentId;

    // Ticket information
    private String verifyCode;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime usedAt;
    private LocalDateTime cancelledAt;

    // Optimistic lock version
    private Integer version;

    // Deferred domain events - published after optimistic lock succeeds
    private final List<OrderDomainEvent> pendingEvents = new ArrayList<>();

    // Deferred side effects to execute after optimistic lock succeeds
    private final List<SideEffect> pendingSideEffects = new ArrayList<>();

    // Builder pattern support
    public static OrderAggregateBuilder builder() {
        return new OrderAggregateBuilder();
    }

    public static class OrderAggregateBuilder {
        private String orderId;
        private String eventId;
        private String venueId;
        private String ticketTypeId;
        private OrderStatus status;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String paymentId;
        private String verifyCode;
        private LocalDateTime createdAt;
        private LocalDateTime paidAt;
        private LocalDateTime usedAt;
        private LocalDateTime cancelledAt;
        private Integer version;

        public OrderAggregateBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public OrderAggregateBuilder eventId(String eventId) { this.eventId = eventId; return this; }
        public OrderAggregateBuilder venueId(String venueId) { this.venueId = venueId; return this; }
        public OrderAggregateBuilder ticketTypeId(String ticketTypeId) { this.ticketTypeId = ticketTypeId; return this; }
        public OrderAggregateBuilder status(OrderStatus status) { this.status = status; return this; }
        public OrderAggregateBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderAggregateBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public OrderAggregateBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public OrderAggregateBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public OrderAggregateBuilder verifyCode(String verifyCode) { this.verifyCode = verifyCode; return this; }
        public OrderAggregateBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderAggregateBuilder paidAt(LocalDateTime paidAt) { this.paidAt = paidAt; return this; }
        public OrderAggregateBuilder usedAt(LocalDateTime usedAt) { this.usedAt = usedAt; return this; }
        public OrderAggregateBuilder cancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; return this; }
        public OrderAggregateBuilder version(Integer version) { this.version = version; return this; }

        public OrderAggregate build() {
            OrderAggregate agg = new OrderAggregate();
            agg.orderId = this.orderId;
            agg.eventId = this.eventId;
            agg.venueId = this.venueId;
            agg.ticketTypeId = this.ticketTypeId;
            agg.status = this.status;
            agg.quantity = this.quantity;
            agg.unitPrice = this.unitPrice;
            agg.totalAmount = this.totalAmount;
            agg.paymentId = this.paymentId;
            agg.verifyCode = this.verifyCode;
            agg.createdAt = this.createdAt;
            agg.paidAt = this.paidAt;
            agg.usedAt = this.usedAt;
            agg.cancelledAt = this.cancelledAt;
            agg.version = this.version;
            return agg;
        }
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getEventId() { return eventId; }
    public String getVenueId() { return venueId; }
    public String getTicketTypeId() { return ticketTypeId; }
    public OrderStatus getStatus() { return status; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentId() { return paymentId; }
    public String getVerifyCode() { return verifyCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public Integer getVersion() { return version; }

    // Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }
    public void setTicketTypeId(String ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public void setVersion(Integer version) { this.version = version; }

    /**
     * Handle PAY command - transition to PAID state
     * 
     * Business rules:
     * - Must be in PENDING_PAY state to receive payment
     * - Amount must match
     */
    public void pay(String paymentId, BigDecimal amount) {
        // Idempotency: if already paid, return gracefully
        if (OrderStatus.PAID.equals(this.status)) {
            return;
        }

        // Business rule: must be in PENDING_PAY state
        if (!OrderStatus.PENDING_PAY.equals(this.status)) {
            throw new IllegalStateException(
                    "Cannot pay order in status: " + this.status);
        }

        // Business rule: amount must match total
        if (!amount.equals(this.totalAmount)) {
            throw new IllegalArgumentException(
                    "Payment amount mismatch. Expected: " + this.totalAmount
                            + ", Received: " + amount);
        }

        // Update aggregate state
        this.status = OrderStatus.PAID;
        this.paymentId = paymentId;
        this.paidAt = LocalDateTime.now();

        // Produce domain event
        this.pendingEvents.add(new OrderPaidEvent(
                this.orderId, paymentId, amount));
    }

    /**
     * Handle TIMEOUT command - transition to TIMEOUT_CANCELLED state
     * 
     * Business rules:
     * - Must be in PENDING_PAY state when timeout occurs
     */
    public void timeout(String reason) {
        // Idempotency: if already in a terminal state related to timeout, return
        if (OrderStatus.TIMEOUT_CANCELLED.equals(this.status)) {
            return;
        }

        // Business rule: timeout only applies to PENDING_PAY orders
        if (!OrderStatus.PENDING_PAY.equals(this.status)) {
            throw new IllegalStateException(
                    "Cannot timeout order in status: " + this.status);
        }

        // Update aggregate state
        this.status = OrderStatus.TIMEOUT_CANCELLED;
        this.cancelledAt = LocalDateTime.now();

        // Produce domain event
        this.pendingEvents.add(new OrderTimeoutEvent(
                this.orderId, reason));
    }

    /**
     * Handle CANCEL command - transition to CANCELLED state
     * 
     * Business rules:
     * - Can only cancel PENDING_PAY orders
     */
    public void cancel(String reason) {
        // Idempotency: if already cancelled, return gracefully
        if (OrderStatus.CANCELLED.equals(this.status)) {
            return;
        }

        // Business rule: can only cancel if pending payment
        if (!OrderStatus.PENDING_PAY.equals(this.status)) {
            throw new IllegalStateException(
                    "Cannot cancel order in status: " + this.status);
        }

        // Update aggregate state
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();

        // Produce domain event
        this.pendingEvents.add(new OrderCancelledEvent(
                this.orderId, reason));
    }

    /**
     * Handle USE command - transition to USED state
     * 
     * Business rules:
     * - Must be in PAID state
     * - Verify code must match
     */
    public void use(String verifyCode) {
        // Idempotency: if already used, return gracefully
        if (OrderStatus.USED.equals(this.status)) {
            return;
        }

        // Business rule: can only use paid orders
        if (!OrderStatus.PAID.equals(this.status)) {
            throw new IllegalStateException(
                    "Cannot use order in status: " + this.status);
        }

        // Business rule: verify code must match
        if (!verifyCode.equals(this.verifyCode)) {
            throw new IllegalArgumentException("Invalid verify code");
        }

        // Update aggregate state
        this.status = OrderStatus.USED;
        this.usedAt = LocalDateTime.now();

        // Produce domain event
        this.pendingEvents.add(new OrderUsedEvent(
                this.orderId, verifyCode));
    }

    /**
     * Handle REFUND command - transition to REFUNDING state
     * 
     * This method handles the race condition scenario where a pay callback
     * arrives after the order has already been cancelled or timed out.
     * The state machine routes to REFUNDING to ensure funds are returned.
     * 
     * Business rules:
     * - Can be called from TIMEOUT_CANCELLED or CANCELLED states
     */
    public void refund(String reason) {
        // Idempotency: if already refunding, return gracefully
        if (OrderStatus.REFUNDING.equals(this.status)) {
            return;
        }

        // Business rule: refund applies to already-cancelled states
        if (!OrderStatus.TIMEOUT_CANCELLED.equals(this.status)
                && !OrderStatus.CANCELLED.equals(this.status)) {
            throw new IllegalStateException(
                    "Cannot refund order in status: " + this.status);
        }

        // Update aggregate state
        this.status = OrderStatus.REFUNDING;

        // Produce domain event
        this.pendingEvents.add(new OrderRefundingEvent(
                this.orderId, reason));
    }

    /**
     * Get all pending domain events and clear the list
     */
    public List<OrderDomainEvent> getPendingEventsAndClear() {
        List<OrderDomainEvent> events = new ArrayList<>(this.pendingEvents);
        this.pendingEvents.clear();
        return events;
    }

    /**
     * Enqueue a side effect to be executed after optimistic lock succeeds
     */
    public void enqueueSideEffect(SideEffect sideEffect) {
        this.pendingSideEffects.add(sideEffect);
    }

    /**
     * Get all pending side effects and clear the list
     */
    public List<SideEffect> getPendingSideEffectsAndClear() {
        List<SideEffect> effects = new ArrayList<>(this.pendingSideEffects);
        this.pendingSideEffects.clear();
        return effects;
    }

    /**
     * Interface for deferred side effects
     * Implementations execute after optimistic lock succeeds
     */
    @FunctionalInterface
    public interface SideEffect {
        void execute();
    }
}
