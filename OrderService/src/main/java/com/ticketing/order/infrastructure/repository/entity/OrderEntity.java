package com.ticketing.order.infrastructure.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order entity - persisted in MySQL database
 * 
 * Includes version field for optimistic locking
 */
@NoArgsConstructor
@Data
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "venue_id")
    private String venueId;

    @Column(name = "ticket_type_id")
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
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Optimistic lock version - incremented on every successful update
    @Version
    private Integer version;

    @Column(name = "updated_at")
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

}
