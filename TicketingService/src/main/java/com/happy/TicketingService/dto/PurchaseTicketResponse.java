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
}
