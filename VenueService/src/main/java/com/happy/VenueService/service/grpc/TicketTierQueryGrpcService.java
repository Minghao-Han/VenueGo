package com.happy.VenueService.service.grpc;

import com.happy.VenueService.entity.TicketTier;
import com.happy.VenueService.repository.TicketTierRepository;
import com.happy.venue.ticket.grpc.GetTicketTierInfoRequest;
import com.happy.venue.ticket.grpc.GetTicketTierInfoResponse;
import com.happy.venue.ticket.grpc.TicketTierQueryServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class TicketTierQueryGrpcService extends TicketTierQueryServiceGrpc.TicketTierQueryServiceImplBase {

    private final TicketTierRepository ticketTierRepository;

    @Override
    public void getTicketTierInfo(GetTicketTierInfoRequest request,
                                  StreamObserver<GetTicketTierInfoResponse> responseObserver) {
        UUID ticketTierId;
        try {
            ticketTierId = UUID.fromString(request.getTicketTierId());
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid ticketTierId: " + request.getTicketTierId())
                    .asRuntimeException());
            return;
        }

        TicketTier tier = this.ticketTierRepository.findById(ticketTierId).orElse(null);
        if (tier == null || tier.getVenue() == null || tier.getPrice() == null) {
            responseObserver.onNext(GetTicketTierInfoResponse.newBuilder()
                    .setFound(false)
                    .setTicketTierId(request.getTicketTierId())
                    .setMessage("Ticket tier not found")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        GetTicketTierInfoResponse response = GetTicketTierInfoResponse.newBuilder()
                .setFound(true)
                .setTicketTierId(request.getTicketTierId())
                .setVenueId(tier.getVenue().getId().toString())
                .setPrice(tier.getPrice().toPlainString())
                .setMessage("OK")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
