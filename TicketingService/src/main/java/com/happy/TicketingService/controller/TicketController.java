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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/tickets")
@Slf4j
public class TicketController {

    private final TicketPurchaseService ticketPurchaseService;

    public TicketController(TicketPurchaseService ticketPurchaseService) {
        this.ticketPurchaseService = ticketPurchaseService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseTicketResponse> purchaseTicket(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody PurchaseTicketRequest request) {
        long startTime = System.currentTimeMillis();
        PurchaseTicketResponse response = ticketPurchaseService.purchase(
                userId,
                request.getTicketTierId(),
                request.getPurchaseCount());
        long endTime = System.currentTimeMillis();
        log.debug("whole processing time {}ms",(endTime - startTime));
        
        if (response.getCode() == null || response.getCode() == 0) {
            // Success
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getCode()).body(response);
        }
    }
}
