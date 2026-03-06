package com.happy.TicketingService.event;

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
public class TicketPurchaseEvent {

    private String messageId;
    private UUID orderId;
    private UUID userId;
    private UUID ticketTierId;
    private Integer purchaseCount;
    private OffsetDateTime occurredAt;
}
