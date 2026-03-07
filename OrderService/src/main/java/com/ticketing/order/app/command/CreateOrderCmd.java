package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Command to create a new order
 */
@Data
public class CreateOrderCmd {

    @NotBlank(message = "Event ID is required")
    private String eventId;

    @NotBlank(message = "Venue ID is required")
    private String venueId;

    @NotBlank(message = "Ticket type ID is required")
    private String ticketTypeId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;

    public CreateOrderCmd() {
    }

    public CreateOrderCmd(String eventId, String venueId, String ticketTypeId, Integer quantity, BigDecimal unitPrice) {
        this.eventId = eventId;
        this.venueId = venueId;
        this.ticketTypeId = ticketTypeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

}

