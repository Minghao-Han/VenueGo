package com.happy.VenueService.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.happy.VenueService.entity.TicketTier;
import com.happy.VenueService.exception.BusinessException;
import com.happy.ticketing.inventory.grpc.DeleteInventoriesRequest;
import com.happy.ticketing.inventory.grpc.DeleteInventoriesResponse;
import com.happy.ticketing.inventory.grpc.InventorySyncServiceGrpc;
import com.happy.ticketing.inventory.grpc.UpsertInventoryRequest;
import com.happy.ticketing.inventory.grpc.UpsertInventoryResponse;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class InventorySyncClient {

    @GrpcClient("inventory-service")
    private InventorySyncServiceGrpc.InventorySyncServiceBlockingStub blockingStub;

    @Retryable(
            retryFor = StatusRuntimeException.class,
            maxAttemptsExpression = "${ticketing.grpc.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${ticketing.grpc.retry.delay-ms:300}", multiplier = 2.0))
    public void upsertInventory(TicketTier tier) {
        if (tier.getSaleStartTime() == null || tier.getSaleEndTime() == null || tier.getPurchaseLimit() == null
            || tier.getTotalCapacity() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                "Ticket tier inventory fields are incomplete for sync: " + tier.getId());
        }

        UpsertInventoryRequest request = UpsertInventoryRequest.newBuilder()
                .setTicketTierId(tier.getId().toString())
                .setAvailableInventory(tier.getTotalCapacity())
                .setPurchaseLimit(tier.getPurchaseLimit())
                .setSaleStartTime(tier.getSaleStartTime().toString())
                .setSaleEndTime(tier.getSaleEndTime().toString())
                .setIdempotencyKey(buildIdempotencyKey(tier))
                .build();

        UpsertInventoryResponse response = blockingStub.upsertInventory(request);
        if (!response.getSuccess()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY,
                    "Inventory sync failed for ticket tier: " + tier.getId());
        }
    }

    @Retryable(
            retryFor = StatusRuntimeException.class,
            maxAttemptsExpression = "${ticketing.grpc.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${ticketing.grpc.retry.delay-ms:300}", multiplier = 2.0))
    public void deleteInventories(List<UUID> ticketTierIds) {
        if (ticketTierIds == null || ticketTierIds.isEmpty()) {
            return;
        }

        DeleteInventoriesRequest request = DeleteInventoriesRequest.newBuilder()
                .addAllTicketTierIds(ticketTierIds.stream().map(UUID::toString).toList())
                .build();

        DeleteInventoriesResponse response = blockingStub.deleteInventories(request);
        if (!response.getSuccess()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY,
                    "Inventory delete failed for ticket tiers: " + ticketTierIds);
        }
    }

    @Recover
    public void recover(StatusRuntimeException ex, TicketTier tier) {
        throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory service unavailable for ticket tier: " + tier.getId() + ", cause: " + ex.getMessage());
    }

    @Recover
    public void recover(StatusRuntimeException ex, List<UUID> ticketTierIds) {
        throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory service unavailable when deleting ticket tiers: " + ticketTierIds + ", cause: "
                        + ex.getMessage());
    }

    private String buildIdempotencyKey(TicketTier tier) {
        return tier.getId()
                + ":"
                + tier.getTotalCapacity()
                + ":"
                + tier.getPurchaseLimit()
                + ":"
                + tier.getSaleStartTime()
                + ":"
                + tier.getSaleEndTime();
    }
}
