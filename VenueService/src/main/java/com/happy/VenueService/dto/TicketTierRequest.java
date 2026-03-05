package com.happy.VenueService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "TicketTierRequest", description = "Ticket tier request")
public class TicketTierRequest {
    @Schema(description = "Tier name", example = "VIP")
    private String tierName;
    @Schema(description = "Ticket price", example = "899.00")
    private BigDecimal price;
    @Schema(description = "Tier capacity", example = "300")
    private Integer totalCapacity;
    @Schema(description = "Sale start time in ISO-8601 with offset", example = "2026-04-01T10:00:00+08:00")
    private OffsetDateTime saleStartTime;
    @Schema(description = "Sale end time in ISO-8601 with offset", example = "2026-05-01T18:00:00+08:00")
    private OffsetDateTime saleEndTime;
}
