package com.happy.TicketingService.grpc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.happy.TicketingService.entity.TicketInventory;
import com.happy.TicketingService.service.InventoryService;
import com.happy.ticketing.inventory.grpc.DeleteInventoriesRequest;
import com.happy.ticketing.inventory.grpc.DeleteInventoriesResponse;
import com.happy.ticketing.inventory.grpc.InventorySyncServiceGrpc;
import com.happy.ticketing.inventory.grpc.UpsertInventoryRequest;
import com.happy.ticketing.inventory.grpc.UpsertInventoryResponse;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Component
public class InventorySyncGrpcService extends InventorySyncServiceGrpc.InventorySyncServiceImplBase {

    private final InventoryService inventoryService;

    public InventorySyncGrpcService(
            InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void upsertInventory(UpsertInventoryRequest request, StreamObserver<UpsertInventoryResponse> responseObserver) {
        try {
            UUID ticketTierId = UUID.fromString(request.getTicketTierId());
                TicketInventory saved = inventoryService.upsert(
                    ticketTierId,
                    request.getAvailableInventory(),
                    request.getPurchaseLimit(),
                    OffsetDateTime.parse(request.getSaleStartTime()),
                    OffsetDateTime.parse(request.getSaleEndTime()));
            responseObserver.onNext(UpsertInventoryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Inventory upserted")
                    .setInventoryId(saved.getId().toString())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteInventories(DeleteInventoriesRequest request,
                                  StreamObserver<DeleteInventoriesResponse> responseObserver) {
        try {
            List<UUID> tierIds = request.getTicketTierIdsList().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            int deletedCount = inventoryService.deleteInventories(tierIds);
            responseObserver.onNext(DeleteInventoriesResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Inventories deleted")
                    .setDeletedCount(deletedCount)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

}
