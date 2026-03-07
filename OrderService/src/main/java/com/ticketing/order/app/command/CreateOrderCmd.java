package com.ticketing.order.app.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Command to create a new order
 */
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

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getVenueId() { return venueId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }

    public String getTicketTypeId() { return ticketTypeId; }
    public void setTicketTypeId(String ticketTypeId) { this.ticketTypeId = ticketTypeId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}

