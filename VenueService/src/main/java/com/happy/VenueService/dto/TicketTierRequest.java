package com.happy.VenueService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketTierRequest {
    private String tierName;
    private BigDecimal price;
    private Integer totalCapacity;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
}
