package com.happy.VenueService.service.impl;

import com.happy.VenueService.config.RedissonConfig;
import com.happy.VenueService.dto.TicketTierRequest;
import com.happy.VenueService.dto.VenueResponse;
import com.happy.VenueService.dto.VenueUpsertRequest;
import com.happy.VenueService.entity.Venue;
import com.happy.VenueService.entity.TicketTier;
import com.happy.VenueService.event.TicketTiersDeletedEvent;
import com.happy.VenueService.exception.BusinessException;
import com.happy.VenueService.repository.VenueRepository;
import com.happy.VenueService.service.InventorySyncClient;
import com.happy.VenueService.service.VenueService;
import com.happy.VenueService.util.Lock.ILock;
import org.redisson.api.RBloomFilter;

import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final StringRedisTemplate redisTemplate;
    private final RBloomFilter<String> venueBloomFilter;
    private final ILock lock;
    private final RedissonConfig redissonConfig;
    private final InventorySyncClient inventorySyncClient;
    private final ApplicationEventPublisher eventPublisher;

    public VenueServiceImpl(VenueRepository venueRepository, 
        StringRedisTemplate redisTemplate, 
        RBloomFilter<String> venueBloomFilter,
        ILock lock,
        RedissonConfig redissonConfig,
        InventorySyncClient inventorySyncClient,
        ApplicationEventPublisher eventPublisher) {
        this.venueRepository = venueRepository;
        this.redisTemplate = redisTemplate;
        this.venueBloomFilter = venueBloomFilter;
        this.lock = lock;
        this.redissonConfig = redissonConfig;
        this.inventorySyncClient = inventorySyncClient;
        this.eventPublisher = eventPublisher;
    }

    private String getCacheKey(UUID venueId) {
        return redissonConfig.getCacheValueKey() + ":" + venueId.toString();
    }

    @Override
    public VenueResponse createVenue(VenueUpsertRequest request, UUID hostId) {
        Venue venue = new Venue();
        venue.setHostId(hostId);
        applyVenueData(venue, request);
        Venue savedVenue = venueRepository.save(venue);
        syncTicketInventories(savedVenue.getTicketTiers());
        redisTemplate.delete(getCacheKey(savedVenue.getId()));
        venueBloomFilter.add(getCacheKey(savedVenue.getId()));
        return VenueResponse.toResponse(savedVenue);
    }

    @Override
    public VenueResponse updateVenue(UUID venueId, VenueUpsertRequest request, UUID hostId) {
        String updateLockKey = "lock:ticket-tier:update:" + venueId;
        boolean locked = lock.tryLock(updateLockKey);
        if (!locked) {
            throw new BusinessException(HttpStatus.CONFLICT, "Venue update is in progress, please retry later");
        }

        try {
            validateTicketTierUpdateWindow(request.getTicketTiers());
            Venue venue = getVenueEntityOrThrow(venueId, hostId);
            List<UUID> oldTierIds = venue.getTicketTiers().stream().map(TicketTier::getId).toList();
            applyVenueData(venue, request);
            Venue updatedVenue = venueRepository.save(venue);
            syncTicketInventories(updatedVenue.getTicketTiers());
            publishDeletedTierEvent(oldTierIds, updatedVenue.getTicketTiers().stream().map(TicketTier::getId).toList());
            redisTemplate.delete(getCacheKey(updatedVenue.getId()));
            return VenueResponse.toResponse(updatedVenue);
        } finally {
            lock.unlock(updateLockKey);
        }
    }

    private void validateTicketTierUpdateWindow(List<TicketTierRequest> ticketTiers) {
        if (ticketTiers == null) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        for (TicketTierRequest tier : ticketTiers) {
            if (tier.getSaleStartTime() == null) {
                continue;
            }
            OffsetDateTime cutoff = tier.getSaleStartTime().minus(1, ChronoUnit.HOURS);
            if (now.isAfter(cutoff)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Ticket tier cannot be updated within one hour before sale start time");
            }
        }
    }

    private void syncTicketInventories(List<TicketTier> tiers) {
        if (tiers == null) {
            return;
        }
        for (TicketTier tier : tiers) {
            inventorySyncClient.upsertInventory(tier);
        }
    }

    @Override
    public void deleteVenue(UUID venueId, UUID hostId) {
        Venue venue = getVenueEntityOrThrow(venueId, hostId);
        List<UUID> deletedTierIds = venue.getTicketTiers().stream().map(TicketTier::getId).toList();
        venueRepository.delete(venue);
        publishDeletedTierEvent(deletedTierIds, List.of());
        redisTemplate.delete(getCacheKey(venue.getId()));
    }

    private void publishDeletedTierEvent(List<UUID> oldTierIds, List<UUID> latestTierIds) {
        if (oldTierIds == null || oldTierIds.isEmpty()) {
            return;
        }

        List<UUID> deletedTierIds = oldTierIds.stream()
                .filter(id -> id != null && (latestTierIds == null || !latestTierIds.contains(id)))
                .toList();

        if (!deletedTierIds.isEmpty()) {
            eventPublisher.publishEvent(new TicketTiersDeletedEvent(deletedTierIds));
        }
    }

    private Venue getVenueEntityOrThrow(UUID venueId, UUID hostId) {
        Venue venue = getVenueByIdOrThrow(venueId);
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

        // Keep both sides of the association consistent for orphan removal.
        venue.clearTicketTiers();
        if (request.getTicketTiers() != null) {
            for (TicketTierRequest tierRequest : request.getTicketTiers()) {
                TicketTier tier = TicketTierRequest.toEntity(tierRequest);
                venue.addTicketTier(tier);
            }
        }
    }

}
