package com.happy.VenueService.repository;

import com.happy.VenueService.entity.Venue;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface VenueRepository extends JpaRepository<Venue, UUID>,JpaSpecificationExecutor<Venue> {
    @Query("SELECT v.id FROM Venue v")
    List<UUID> findAllIds();
}
