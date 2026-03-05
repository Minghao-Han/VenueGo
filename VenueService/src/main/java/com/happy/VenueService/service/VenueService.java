package com.happy.VenueService.service;

import com.happy.VenueService.controller.VenueFilter;
import com.happy.VenueService.dto.VenueResponse;
import com.happy.VenueService.dto.VenueUpsertRequest;
import com.happy.VenueService.entity.Venue;

import java.util.UUID;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

public interface VenueService {
    VenueResponse createVenue(VenueUpsertRequest request, UUID hostId);

    VenueResponse updateVenue(UUID venueId, VenueUpsertRequest request, UUID hostId);

    void deleteVenue(UUID venueId, UUID hostId);
    Venue getVenueById(UUID venueId);
    Window<Venue> getVenues(VenueFilter filter,ScrollPosition position, Integer first);
}
