package com.happy.TicketingService.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PurchaseTicketRequest {

    @NotNull(message = "ticketTierId is required")
    private UUID ticketTierId;

    @NotNull(message = "purchaseCount is required")
    @Min(value = 1, message = "purchaseCount must be at least 1")
    private Integer purchaseCount = 1;
}
