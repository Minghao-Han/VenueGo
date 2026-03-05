package com.happy.VenueService.service.impl;

import com.happy.VenueService.controller.VenueFilter;
import com.happy.VenueService.dto.TicketTierRequest;
import com.happy.VenueService.dto.TicketTierResponse;
import com.happy.VenueService.dto.VenueResponse;
import com.happy.VenueService.dto.VenueUpsertRequest;
import com.happy.VenueService.entity.TicketTier;
import com.happy.VenueService.entity.Venue;
import com.happy.VenueService.exception.BusinessException;
import com.happy.VenueService.repository.VenueRepository;
import com.happy.VenueService.service.VenueService;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    public VenueServiceImpl(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    public VenueResponse createVenue(VenueUpsertRequest request, UUID hostId) {
        Venue venue = new Venue();
        venue.setHostId(hostId);
        applyVenueData(venue, request);
        return toResponse(venueRepository.save(venue));
    }

    @Override
    public VenueResponse updateVenue(UUID venueId, VenueUpsertRequest request, UUID hostId) {
        Venue venue = getVenueEntityOrThrow(venueId, hostId);
        applyVenueData(venue, request);
        return toResponse(venueRepository.save(venue));
    }

    @Override
    public void deleteVenue(UUID venueId, UUID hostId) {
        Venue venue = getVenueEntityOrThrow(venueId, hostId);
        venueRepository.delete(venue);
    }

    private Venue getVenueEntityOrThrow(UUID venueId, UUID hostId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId));
        if (!hostId.equals(venue.getHostId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No permission to modify this venue");
        }
        return venue;
    }

    private Venue getVenueByIdOrThrow(UUID venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId));
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
                .hostId(venue.getHostId())
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

    @Override
    public Venue getVenueById(UUID venueId) {
        return getVenueByIdOrThrow(venueId);
    }

    @Override
    public Window<Venue> getVenues(VenueFilter filter, ScrollPosition position, Integer first) {
        VenueFilter safeFilter = filter != null ? filter : new VenueFilter();
        Specification<Venue> spec = Specification
            .where(VenueSpecifications.hasCity(safeFilter.getCityCode()))
            .and(VenueSpecifications.minPriceGreaterThan(safeFilter.getMinPrice()))
            .and(VenueSpecifications.maxPriceLessThan(safeFilter.getMaxPrice()))
            .and(VenueSpecifications.hasStartTimeBefore(safeFilter.getStartTimeBefore()))
            .and(VenueSpecifications.hasStartTimeAfter(safeFilter.getStartTimeAfter()))
            .and(VenueSpecifications.hasCategoryIn(safeFilter.getCategories()));

        return venueRepository.findBy(spec, q -> q.sortBy(Sort.by("id").ascending())
            .limit(first != null ? first : 10)
            .scroll(position));
    }
}
