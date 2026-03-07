package com.ticketing.order.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Order
 * Used in REST API responses
 */
@Data
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

}

