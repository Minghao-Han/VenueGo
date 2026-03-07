package com.happy.VenueService.service.impl;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.type.TypeReference;

import com.happy.VenueService.config.RedissonConfig;
import com.happy.VenueService.controller.VenueFilter;
import com.happy.VenueService.dto.RedisData;
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
import com.happy.VenueService.util.Random.IRandom;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBloomFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@Slf4j
@ConditionalOnProperty(prefix = "venue.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final StringRedisTemplate redisTemplate;
    private final RBloomFilter<String> venueBloomFilter;
    private final IRandom random;
    private final JsonMapper objectMapper;
    private final ILock lock;
    private final RedissonConfig redissonConfig;
    private final InventorySyncClient inventorySyncClient;
    private final ApplicationEventPublisher eventPublisher;

    public VenueServiceImpl(VenueRepository venueRepository, 
        StringRedisTemplate redisTemplate, 
        RBloomFilter<String> venueBloomFilter,
        IRandom random, 
        ILock lock,
        JsonMapper objectMapper,
        RedissonConfig redissonConfig,
        InventorySyncClient inventorySyncClient,
        ApplicationEventPublisher eventPublisher) {
        this.venueRepository = venueRepository;
        this.redisTemplate = redisTemplate;
        this.venueBloomFilter = venueBloomFilter;
        this.random = random;
        this.lock = lock;
        this.objectMapper = objectMapper;
        this.redissonConfig = redissonConfig;
        this.inventorySyncClient = inventorySyncClient;
        this.eventPublisher = eventPublisher;
    }

    private String getCacheKey(UUID venueId) {
        return redissonConfig.getCacheValueKey() + ":" + venueId.toString();
    }

    private String getLockKey(UUID venueId) {
        return redissonConfig.getLockValueKey() + ":" + venueId.toString();
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

    @Override
    public Venue getVenueById(UUID venueId) {
        return getVenueWithCache(venueId);
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

    
    // Read from Redis first.
    // If value exists and is not logically expired, return it.
    // If value is missing, check bloom filter before attempting database fallback.
    // Use a distributed lock to prevent cache breakdown under concurrency.
    private Venue getVenueWithCache(UUID venueId) {
        String key = getCacheKey(venueId);
        log.debug("testing cache for venue");
        
        // 1. Read from Redis.
        String json = redisTemplate.opsForValue().get(key);
        RedisData<Venue> cachedData = null;
        
        // 2. Handle cache hit.
        if (StringUtils.hasText(json)) {
            log.debug("Cache hit");
            try {
                cachedData = readRedisData(json);
            } catch (Exception ex) {
                // Backward compatibility for legacy malformed cache values (e.g. data="").
                log.warn("Invalid venue cache payload, fallback to DB, key={}", key, ex);
                redisTemplate.delete(key);
                cachedData = null;
            }

            if (cachedData != null) {
                Venue venue = cachedData.getData();
                LocalDateTime expireTime = cachedData.getExpireTime();
            
                // 2.1 Return directly when not logically expired.
                if (expireTime.isAfter(LocalDateTime.now())) {
                    if (venue == null) {
                        throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
                    }
                    return venue;
                }
                // 2.2 Continue into lock flow if logically expired.
            }
        } else {
            // 3. Cache miss, check bloom filter.
            if (!venueBloomFilter.contains(key)) {
                log.debug("Bloom filter negative for key: {}", key);
                throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
            }
            // Bloom filter indicates possible existence, continue to lock and DB fallback.
        }

        // 4. Acquire lock.
        String lockKey = getLockKey(venueId);
        boolean isLock = lock.tryLock(lockKey);
        
        try {
            if (!isLock) {
                // 4.1 Lock not acquired.
                log.debug("Failed to acquire lock for venue: {}", venueId);
                if (cachedData != null) {
                    // Return stale data when the cached entry was logically expired.
                    Venue staleVenue = cachedData.getData();
                    if (staleVenue != null) {
                        return staleVenue;
                    }
                }
                throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
            }

            // 4.2 Lock acquired. A second cache check can be enabled if needed.
            // String latestJson = redisTemplate.opsForValue().get(key);
            // if (StringUtils.hasText(latestJson)) {
            //     RedisData<Venue> latestData = readRedisData(latestJson);
            //     if (latestData.getExpireTime().isAfter(LocalDateTime.now())) {
            //         return latestData.getData();
            //     }
            // }

            // 5. Query the database.
            log.debug("Querying database for venue: {}", venueId);
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if (venueOptional.isEmpty()) {
                saveToCache(key, null, 10);
                throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
            }

            // 6. Write entity into logical-expiration cache.
            Venue venue = venueOptional.get();        
            saveToCache(key, venue);
            return venue;

        } finally {
            // 7. Release lock.
            if (isLock) {
                lock.unlock(lockKey);
            }
        }
    }

    private RedisData<Venue> readRedisData(String json) {
        return objectMapper.readValue(json, new TypeReference<RedisData<Venue>>() {});
    }

    private <T> void saveToCache(String key, T value, long baseLogicExpireMinute) {
        // Add random jitter to logical expiration to reduce stampede risk.
        long randomMinutes = random.nextLong(1, 10);
        RedisData<T> data = new RedisData<>();
        data.setData(value);
        data.setExpireTime(LocalDateTime.now().plusMinutes(baseLogicExpireMinute + randomMinutes));
        
        // Keep a large physical TTL while relying on logical expiration.
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), 1, TimeUnit.DAYS);
        
    }
    private <T> void saveToCache(String key, T value) {
        saveToCache(key,value,30);
    }
}
