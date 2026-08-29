package com.happy.VenueService.service.impl;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import com.happy.VenueService.config.RedissonConfig;
import com.happy.VenueService.controller.VenueFilter;
import com.happy.VenueService.dto.RedisData;
import com.happy.VenueService.entity.Venue;
import com.happy.VenueService.exception.BusinessException;
import com.happy.VenueService.repository.VenueRepository;
import com.happy.VenueService.service.VenueQueryService;
import com.happy.VenueService.util.Lock.ILock;
import com.happy.VenueService.util.Random.IRandom;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RBloomFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("rawVenueCachedQueryService")
@Slf4j
@ConditionalOnProperty(prefix = "venue.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VenueCachedQueryServiceImpl implements VenueQueryService {

    private final VenueRepository venueRepository;
    private final StringRedisTemplate redisTemplate;
    private final RBloomFilter<String> venueBloomFilter;
    private final IRandom random;
    private final JsonMapper objectMapper;
    private final ILock lock;
    private final RedissonConfig redissonConfig;

    public VenueCachedQueryServiceImpl(VenueRepository venueRepository,
                                       StringRedisTemplate redisTemplate,
                                       RBloomFilter<String> venueBloomFilter,
                                       IRandom random,
                                       ILock lock,
                                       JsonMapper objectMapper,
                                       RedissonConfig redissonConfig) {
        this.venueRepository = venueRepository;
        this.redisTemplate = redisTemplate;
        this.venueBloomFilter = venueBloomFilter;
        this.random = random;
        this.lock = lock;
        this.objectMapper = objectMapper;
        this.redissonConfig = redissonConfig;
    }

    private String getCacheKey(UUID venueId) {
        return redissonConfig.getCacheValueKey() + ":" + venueId;
    }

    private String getLockKey(UUID venueId) {
        return redissonConfig.getLockValueKey() + ":" + venueId;
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

    private Venue getVenueWithCache(UUID venueId) {
        String key = getCacheKey(venueId);
        log.debug("testing cache for venue");

        String json = redisTemplate.opsForValue().get(key);
        RedisData<Venue> cachedData = null;

        if (StringUtils.hasText(json)) {
            log.debug("Cache hit");
            try {
                cachedData = readRedisData(json);
            } catch (Exception ex) {
                log.warn("Invalid venue cache payload, fallback to DB, key={}", key, ex);
                redisTemplate.delete(key);
                cachedData = null;
            }

            if (cachedData != null) {
                Venue venue = cachedData.getData();
                LocalDateTime expireTime = cachedData.getExpireTime();

                if (expireTime.isAfter(LocalDateTime.now())) {
                    if (venue == null) {
                        throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
                    }
                    return venue;
                }
            }
        } else if (!venueBloomFilter.contains(key)) {
            log.debug("Bloom filter negative for key: {}", key);
            throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
        }

        String lockKey = getLockKey(venueId);
        boolean isLock = lock.tryLock(lockKey);

        try {
            if (!isLock) {
                log.debug("Failed to acquire lock for venue: {}", venueId);
                if (cachedData != null) {
                    Venue staleVenue = cachedData.getData();
                    if (staleVenue != null) {
                        return staleVenue;
                    }
                }
                throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
            }

            log.debug("Querying database for venue: {}", venueId);
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if (venueOptional.isEmpty()) {
                saveToCache(key, null, 10);
                throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
            }

            Venue venue = venueOptional.get();
            saveToCache(key, venue);
            return venue;

        } finally {
            if (isLock) {
                lock.unlock(lockKey);
            }
        }
    }

    private RedisData<Venue> readRedisData(String json) {
        return objectMapper.readValue(json, new TypeReference<RedisData<Venue>>() {});
    }

    private <T> void saveToCache(String key, T value, long baseLogicExpireMinute) {
        long randomMinutes = random.nextLong(1, 10);
        RedisData<T> data = new RedisData<>();
        data.setData(value);
        data.setExpireTime(LocalDateTime.now().plusMinutes(baseLogicExpireMinute + randomMinutes));
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), 1, TimeUnit.DAYS);
    }

    private <T> void saveToCache(String key, T value) {
        saveToCache(key, value, 30);
    }
}