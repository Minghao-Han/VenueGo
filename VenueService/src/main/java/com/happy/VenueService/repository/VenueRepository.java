package com.happy.VenueService.repository;

import com.happy.VenueService.entity.Venue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
}
