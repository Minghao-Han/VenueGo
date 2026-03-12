package com.happy.TicketingService.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseTicketResponse {

    private UUID orderId;
    private UUID ticketTierId;
    private Integer purchaseCount;
    private String prepayCode;

    // Status fields
    private Integer code;  // 0 for success, non-zero for failure
    private String message;  // Error message if code != 0
}
