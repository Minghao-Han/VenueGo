package com.happy.VenueService.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import com.happy.VenueService.controller.VenueFilter;
import com.happy.VenueService.entity.Venue;
import com.happy.VenueService.service.VenueQueryService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "venue.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "venue.cache", name = "level", havingValue = "L2")
@CacheConfig(cacheNames = "venueById")
public class VenueCaffeineQueryServiceImpl implements VenueQueryService  {

    private final VenueCachedQueryServiceImpl cachedQueryService;

    public VenueCaffeineQueryServiceImpl(@Qualifier("rawVenueCachedQueryService") VenueCachedQueryServiceImpl cachedQueryService) {
        this.cachedQueryService = cachedQueryService;
    }

    @Override
    @Cacheable(key = "#venueId")
    public Venue getVenueById(UUID venueId) {
        return cachedQueryService.getVenueById(venueId);
    }

    @Override
    @Cacheable(cacheNames = "venuesPage", key = "T(java.util.Objects).hash(#filter, #position, #first)")
    public Window<Venue> getVenues(VenueFilter filter, ScrollPosition position, Integer first) {
        return cachedQueryService.getVenues(filter, position, first);
    }

}
