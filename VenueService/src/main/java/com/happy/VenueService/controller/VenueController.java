package com.happy.VenueService.controller;

import com.happy.VenueService.dto.VenueResponse;
import com.happy.VenueService.dto.VenueUpsertRequest;
import com.happy.VenueService.service.VenueService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VenueResponse createVenue(@RequestBody VenueUpsertRequest request) {
        return venueService.createVenue(request);
    }

    @PutMapping("/{venueId}")
    public VenueResponse updateVenue(@PathVariable UUID venueId, @RequestBody VenueUpsertRequest request) {
        return venueService.updateVenue(venueId, request);
    }

    @DeleteMapping("/{venueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVenue(@PathVariable UUID venueId) {
        venueService.deleteVenue(venueId);
    }
}
