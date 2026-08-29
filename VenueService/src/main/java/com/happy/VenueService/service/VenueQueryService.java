package com.happy.VenueService.service;

import com.happy.VenueService.controller.VenueFilter;
import com.happy.VenueService.entity.Venue;

import java.util.UUID;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

public interface VenueQueryService {
    Venue getVenueById(UUID venueId);

    Window<Venue> getVenues(VenueFilter filter, ScrollPosition position, Integer first);
}