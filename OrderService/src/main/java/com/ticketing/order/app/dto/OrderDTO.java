package com.ticketing.order.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Order
 * Used in REST API responses
 */
public class OrderDTO {

    private String orderId;
    private String eventId;
    private String venueId;
    private String ticketTypeId;

    private String status;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;

    private String paymentId;
    private String verifyCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime usedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelledAt;

    private Integer version;

    // Constructors
    public OrderDTO() {
    }

    public OrderDTO(String orderId, String eventId, String venueId, String ticketTypeId,
                    String status, Integer quantity, BigDecimal unitPrice, BigDecimal totalAmount,
                    String paymentId, String verifyCode, LocalDateTime createdAt,
                    LocalDateTime paidAt, LocalDateTime usedAt, LocalDateTime cancelledAt,
                    Integer version) {
        this.orderId = orderId;
        this.eventId = eventId;
        this.venueId = venueId;
        this.ticketTypeId = ticketTypeId;
        this.status = status;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.paymentId = paymentId;
        this.verifyCode = verifyCode;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.usedAt = usedAt;
        this.cancelledAt = cancelledAt;
        this.version = version;
    }

    // Getters
    public String getOrderId() { return orderId; }
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

    // Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
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
}

