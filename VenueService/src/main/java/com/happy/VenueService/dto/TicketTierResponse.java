package com.happy.VenueService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TicketTierResponse", description = "Ticket tier response")
public class TicketTierResponse {
    @Schema(description = "Ticket tier UUID")
    private UUID id;
    @Schema(description = "Tier name")
    private String tierName;
    @Schema(description = "Ticket price")
    private BigDecimal price;
    @Schema(description = "Tier capacity")
    private Integer totalCapacity;
    @Schema(description = "Per-user purchase limit")
    private Integer purchaseLimit;
    @Schema(description = "Sale start time in ISO-8601 with offset")
    private OffsetDateTime saleStartTime;
    @Schema(description = "Sale end time in ISO-8601 with offset")
    private OffsetDateTime saleEndTime;
}
