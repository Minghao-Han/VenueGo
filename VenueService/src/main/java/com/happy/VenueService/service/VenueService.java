package com.happy.VenueService.service;

import com.happy.VenueService.dto.VenueResponse;
import com.happy.VenueService.dto.VenueUpsertRequest;

import java.util.UUID;

public interface VenueService {
    VenueResponse createVenue(VenueUpsertRequest request, UUID hostId);

    VenueResponse updateVenue(UUID venueId, VenueUpsertRequest request, UUID hostId);

    void deleteVenue(UUID venueId, UUID hostId);
}
