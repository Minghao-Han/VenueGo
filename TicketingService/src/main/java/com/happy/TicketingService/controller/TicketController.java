package com.happy.TicketingService.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.happy.TicketingService.dto.PurchaseTicketRequest;
import com.happy.TicketingService.dto.PurchaseTicketResponse;
import com.happy.TicketingService.service.TicketPurchaseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketPurchaseService ticketPurchaseService;

    public TicketController(TicketPurchaseService ticketPurchaseService) {
        this.ticketPurchaseService = ticketPurchaseService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseTicketResponse> purchaseTicket(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody PurchaseTicketRequest request) {
        PurchaseTicketResponse response = ticketPurchaseService.purchase(
                userId,
                request.getTicketTierId(),
                request.getPurchaseCount());
        return ResponseEntity.ok(response);
    }
}
