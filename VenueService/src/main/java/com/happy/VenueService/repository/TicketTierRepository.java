package com.happy.VenueService.repository;

import com.happy.VenueService.entity.TicketTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketTierRepository extends JpaRepository<TicketTier, UUID> {
}
