package com.happy.VenueService.service.impl;

import com.happy.VenueService.dto.TicketTierRequest;
import com.happy.VenueService.dto.TicketTierResponse;
import com.happy.VenueService.dto.VenueResponse;
import com.happy.VenueService.dto.VenueUpsertRequest;
import com.happy.VenueService.entity.TicketTier;
import com.happy.VenueService.entity.Venue;
import com.happy.VenueService.repository.VenueRepository;
import com.happy.VenueService.service.VenueService;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    public VenueServiceImpl(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    public VenueResponse createVenue(VenueUpsertRequest request) {
        Venue venue = new Venue();
        applyVenueData(venue, request);
        return toResponse(venueRepository.save(venue));
    }

    @Override
    public VenueResponse updateVenue(UUID venueId, VenueUpsertRequest request) {
        Venue venue = getVenueEntityOrThrow(venueId);
        applyVenueData(venue, request);
        return toResponse(venueRepository.save(venue));
    }

    @Override
    public void deleteVenue(UUID venueId) {
        Venue venue = getVenueEntityOrThrow(venueId);
        venueRepository.delete(venue);
    }

    private Venue getVenueEntityOrThrow(UUID venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId));
    }

    private void applyVenueData(Venue venue, VenueUpsertRequest request) {
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCityCode(request.getCityCode());
        venue.setLatitude(request.getLatitude());
        venue.setLongitude(request.getLongitude());
        venue.setDescription(request.getDescription());
        venue.setCapacity(request.getCapacity());
        venue.setStartTime(request.getStartTime());
        venue.setEndTime(request.getEndTime());
        venue.setPosterUrl(request.getPosterUrl());

        if (request.getStatus() != null) {
            venue.setStatus(request.getStatus());
        }

        // Clear-and-fill keeps orphanRemoval effective for ticket tier updates.
        venue.getTicketTiers().clear();
        if (request.getTicketTiers() != null) {
            venue.getTicketTiers().addAll(request.getTicketTiers().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList()));
        }
    }

    private TicketTier toEntity(TicketTierRequest request) {
        TicketTier tier = new TicketTier();
        tier.setTierName(request.getTierName());
        tier.setPrice(request.getPrice());
        tier.setTotalCapacity(request.getTotalCapacity());
        tier.setSaleStartTime(request.getSaleStartTime());
        tier.setSaleEndTime(request.getSaleEndTime());
        return tier;
    }

    private VenueResponse toResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .cityCode(venue.getCityCode())
                .latitude(venue.getLatitude())
                .longitude(venue.getLongitude())
                .description(venue.getDescription())
                .capacity(venue.getCapacity())
                .startTime(venue.getStartTime())
                .endTime(venue.getEndTime())
                .posterUrl(venue.getPosterUrl())
                .status(venue.getStatus())
                .createdAt(venue.getCreatedAt())
                .updatedAt(venue.getUpdatedAt())
                .ticketTiers(venue.getTicketTiers().stream()
                        .map(tier -> TicketTierResponse.builder()
                                .id(tier.getId())
                                .tierName(tier.getTierName())
                                .price(tier.getPrice())
                                .totalCapacity(tier.getTotalCapacity())
                            .saleStartTime(tier.getSaleStartTime())
                            .saleEndTime(tier.getSaleEndTime())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
