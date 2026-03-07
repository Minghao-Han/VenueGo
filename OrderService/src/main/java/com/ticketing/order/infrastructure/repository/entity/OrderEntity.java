package com.ticketing.order.infrastructure.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order entity - persisted in MySQL database
 * 
 * Includes version field for optimistic locking
 */
@NoArgsConstructor
@TableName("orders")
public class OrderEntity {

    @TableId
    private String id;

    private String eventId;
    private String venueId;
    private String ticketTypeId;

    // Order state
    private String status;
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

    // Optimistic lock version - incremented on every successful update
    private Integer version;

    private LocalDateTime updatedAt;

    // Builder pattern support
    public static OrderEntityBuilder builder() {
        return new OrderEntityBuilder();
    }

    public static class OrderEntityBuilder {
        private String id;
        private String eventId;
        private String venueId;
        private String ticketTypeId;
        private String status;
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
        private LocalDateTime updatedAt;

        public OrderEntityBuilder id(String id) { this.id = id; return this; }
        public OrderEntityBuilder eventId(String eventId) { this.eventId = eventId; return this; }
        public OrderEntityBuilder venueId(String venueId) { this.venueId = venueId; return this; }
        public OrderEntityBuilder ticketTypeId(String ticketTypeId) { this.ticketTypeId = ticketTypeId; return this; }
        public OrderEntityBuilder status(String status) { this.status = status; return this; }
        public OrderEntityBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderEntityBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public OrderEntityBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public OrderEntityBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public OrderEntityBuilder verifyCode(String verifyCode) { this.verifyCode = verifyCode; return this; }
        public OrderEntityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderEntityBuilder paidAt(LocalDateTime paidAt) { this.paidAt = paidAt; return this; }
        public OrderEntityBuilder usedAt(LocalDateTime usedAt) { this.usedAt = usedAt; return this; }
        public OrderEntityBuilder cancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; return this; }
        public OrderEntityBuilder version(Integer version) { this.version = version; return this; }
        public OrderEntityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OrderEntity build() {
            OrderEntity entity = new OrderEntity();
            entity.id = this.id;
            entity.eventId = this.eventId;
            entity.venueId = this.venueId;
            entity.ticketTypeId = this.ticketTypeId;
            entity.status = this.status;
            entity.quantity = this.quantity;
            entity.unitPrice = this.unitPrice;
            entity.totalAmount = this.totalAmount;
            entity.paymentId = this.paymentId;
            entity.verifyCode = this.verifyCode;
            entity.createdAt = this.createdAt;
            entity.paidAt = this.paidAt;
            entity.usedAt = this.usedAt;
            entity.cancelledAt = this.cancelledAt;
            entity.version = this.version;
            entity.updatedAt = this.updatedAt;
            return entity;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getVenueId() { return venueId; }
    public String getTicketTypeId() { return ticketTypeId; }
    public String getStatus() { return status; }
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
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }
    public void setTicketTypeId(String ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public void setStatus(String status) { this.status = status; }
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
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
