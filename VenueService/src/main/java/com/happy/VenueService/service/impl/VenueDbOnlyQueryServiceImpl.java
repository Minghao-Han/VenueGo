package com.happy.VenueService.service.impl;

import com.happy.VenueService.controller.VenueFilter;
import com.happy.VenueService.entity.Venue;
import com.happy.VenueService.exception.BusinessException;
import com.happy.VenueService.repository.VenueRepository;
import com.happy.VenueService.service.VenueQueryService;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@ConditionalOnProperty(prefix = "venue.cache", name = "enabled", havingValue = "false")
public class VenueDbOnlyQueryServiceImpl implements VenueQueryService {

    private final VenueRepository venueRepository;

    public VenueDbOnlyQueryServiceImpl(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    public Venue getVenueById(UUID venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId));
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