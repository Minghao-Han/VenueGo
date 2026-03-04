package com.happy.VenueService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTierResponse {
    private UUID id;
    private String tierName;
    private BigDecimal price;
    private Integer totalCapacity;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
}
