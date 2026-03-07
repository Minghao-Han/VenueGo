package com.ticketing.order.infrastructure.grpc;

import com.happy.venue.ticket.grpc.GetTicketTierInfoRequest;
import com.happy.venue.ticket.grpc.GetTicketTierInfoResponse;
import com.happy.venue.ticket.grpc.TicketTierQueryServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class VenueTicketTierQueryClient {

    @GrpcClient("venue-service")
    private TicketTierQueryServiceGrpc.TicketTierQueryServiceBlockingStub blockingStub;

    public TicketTierInfo queryTicketTierInfo(UUID ticketTierId) {
        GetTicketTierInfoRequest request = GetTicketTierInfoRequest.newBuilder()
                .setTicketTierId(ticketTierId.toString())
                .build();

        try {
            GetTicketTierInfoResponse response = this.blockingStub.getTicketTierInfo(request);
            if (!response.getFound()) {
                throw new IllegalArgumentException("Ticket tier not found: " + ticketTierId);
            }

            UUID venueId = UUID.fromString(response.getVenueId());
            BigDecimal price = new BigDecimal(response.getPrice());
            return new TicketTierInfo(venueId, price);
        } catch (StatusRuntimeException e) {
            log.error("gRPC request to VenueService failed for ticketTierId={}", ticketTierId, e);
            throw new RuntimeException("Failed to query ticket tier info from VenueService", e);
        }
    }

    public record TicketTierInfo(UUID venueId, BigDecimal price) {
    }
}
