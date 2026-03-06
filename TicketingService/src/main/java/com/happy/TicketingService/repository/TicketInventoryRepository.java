package com.happy.TicketingService.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.happy.TicketingService.entity.TicketInventory;

public interface TicketInventoryRepository extends JpaRepository<TicketInventory, UUID> {

    Optional<TicketInventory> findByTicketTierId(UUID ticketTierId);

    int deleteByTicketTierId(UUID ticketTierId);

    List<TicketInventory> findBySaleEndTimeAfter(OffsetDateTime now);

    @Modifying
    @Query(value = """
            UPDATE ticket_inventories
            SET available_inventory = available_inventory - :count
            WHERE id = :id
              AND available_inventory >= :count
            """, nativeQuery = true)
    int decreaseAvailableInventoryIfEnough(@Param("id") UUID id, @Param("count") int count);
    @Modifying
    @Query(value = """
            UPDATE ticket_inventories
            SET available_inventory = available_inventory - :count
            WHERE ticket_tier_id = :ticketTierId
              AND available_inventory >= :count
            """, nativeQuery = true)
    int decreaseAvailableInventoryIfEnoughByTicketTierId(@Param("ticketTierId") UUID ticketTierId, @Param("count") int count);
}
