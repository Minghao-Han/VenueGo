package com.happy.VenueService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.happy.VenueService.entity.TicketTier;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @Schema(description = "Per-user purchase limit", example = "2")
    @NotNull(message = "purchaseLimit is required")
    @Min(value = 1, message = "purchaseLimit must be at least 1")
    private Integer purchaseLimit;
    @Schema(description = "Sale start time in ISO-8601 with offset", example = "2026-04-01T10:00:00+08:00")
    private OffsetDateTime saleStartTime;
    @Schema(description = "Sale end time in ISO-8601 with offset", example = "2026-05-01T18:00:00+08:00")
    private OffsetDateTime saleEndTime;
    
    public static TicketTier toEntity(TicketTierRequest request) {
        TicketTier tier = new TicketTier();
        tier.setTierName(request.getTierName());
        tier.setPrice(request.getPrice());
        tier.setTotalCapacity(request.getTotalCapacity());
        tier.setPurchaseLimit(request.getPurchaseLimit());
        tier.setSaleStartTime(request.getSaleStartTime());
        tier.setSaleEndTime(request.getSaleEndTime());
        return tier;
    }
}
