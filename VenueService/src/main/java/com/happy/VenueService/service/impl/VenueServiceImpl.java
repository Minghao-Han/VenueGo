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
import com.happy.VenueService.exception.BusinessException;
import com.happy.VenueService.repository.VenueRepository;
import com.happy.VenueService.service.VenueService;
import com.happy.VenueService.util.Lock.ILock;
import com.happy.VenueService.util.Random.IRandom;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RBloomFilter;
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
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final StringRedisTemplate redisTemplate;
    private final RBloomFilter<String> venueBloomFilter;
    private final IRandom random;
    private final JsonMapper objectMapper;
    private final ILock lock;
    private final RedissonConfig redissonConfig;

    public VenueServiceImpl(VenueRepository venueRepository, 
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
        redisTemplate.delete(getCacheKey(savedVenue.getId()));
        venueBloomFilter.add(getCacheKey(savedVenue.getId()));
        return VenueResponse.toResponse(savedVenue);
    }

    @Override
    public VenueResponse updateVenue(UUID venueId, VenueUpsertRequest request, UUID hostId) {
        Venue venue = getVenueEntityOrThrow(venueId, hostId);
        applyVenueData(venue, request);
        Venue updatedVenue = venueRepository.save(venue);
        redisTemplate.delete(getCacheKey(updatedVenue.getId()));
        return VenueResponse.toResponse(updatedVenue);
    }

    @Override
    public void deleteVenue(UUID venueId, UUID hostId) {
        Venue venue = getVenueEntityOrThrow(venueId, hostId);
        venueRepository.delete(venue);
        redisTemplate.delete(getCacheKey(venue.getId()));
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

        // Clear-and-fill keeps orphanRemoval effective for ticket tier updates.
        venue.getTicketTiers().clear();
        if (request.getTicketTiers() != null) {
            venue.getTicketTiers().addAll(request.getTicketTiers().stream()
                    .map(TicketTierRequest::toEntity)
                    .collect(Collectors.toList()));
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

    
    // get from redis
    // if not null and not expired, return it
    // if null
        // check bloom filter
            // if not exist, throw not found
            // if exist, continue
    // request lock
        // if not acquired, return null/old data
        // if acquired, query db, set cache, release lock, return result
    private Venue getVenueWithCache(UUID venueId) {
        String key = getCacheKey(venueId);
        log.debug("testing cache for venue");
        
        // 1. 从 Redis 获取
        String json = redisTemplate.opsForValue().get(key);
        
        // 2. 命中判断
        if (StringUtils.hasText(json)) {
            log.debug("Cache hit");
            RedisData<Venue> redisData = readRedisData(json);
            Venue venue = redisData.getData();
            LocalDateTime expireTime = redisData.getExpireTime();
            
            // 2.1 未逻辑过期，直接返回
            if (expireTime.isAfter(LocalDateTime.now())) {
                return venue;
            }
            // 2.2 已过期，准备进入加锁逻辑（尝试更新）
        } else {
            // 3. 缓存为空，检查布隆过滤器
            if (!venueBloomFilter.contains(key)) {
                log.debug("Bloom filter negative for key: {}", key);
                throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
            }
            // 布隆过滤器说存在，准备加锁去查 DB
        }

        // 4. 获取互斥锁
        String lockKey = getLockKey(venueId);
        boolean isLock = lock.tryLock(lockKey);
        
        try {
            if (!isLock) {
                // 4.1 获取锁失败
                log.debug("Failed to acquire lock for venue: {}", venueId);
                if (StringUtils.hasText(json)) {
                    // 如果是逻辑过期，返回旧数据
                    return readRedisData(json).getData();
                }
                throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
            }

            // 4.2 获取锁成功，Double Check (再次检查缓存，防止重复查库)
            // String latestJson = redisTemplate.opsForValue().get(key);
            // if (StringUtils.hasText(latestJson)) {
            //     RedisData<Venue> latestData = readRedisData(latestJson);
            //     if (latestData.getExpireTime().isAfter(LocalDateTime.now())) {
            //         return latestData.getData();
            //     }
            // }

            // 5. 同步查询数据库
            log.debug("Querying database for venue: {}", venueId);
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if (venueOptional.isEmpty()) {
                saveToCache(key,"",10);
                throw new BusinessException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
            }

            // 6. 数据库搜到了，正常写入逻辑过期缓存
            Venue venue = venueOptional.get();        
            saveToCache(key, venue);
            return venue;

        } finally {
            // 7. 释放锁
            if (isLock) {
                lock.unlock(lockKey);
            }
        }
    }

    private RedisData<Venue> readRedisData(String json) {
        return objectMapper.readValue(json, new TypeReference<RedisData<Venue>>() {});
    }

    private <T> void saveToCache(String key, T value, long baseLogicExpireMinute) {
        // 使用随机增加过期时间，防止雪崩
        // 假设基础逻辑过期时间为 30 分钟，随机增加 1-10 分钟
        long randomMinutes = random.nextLong(1, 10);
        RedisData<T> data = new RedisData<>();
        data.setData(value);
        data.setExpireTime(LocalDateTime.now().plusMinutes(baseLogicExpireMinute + randomMinutes));
        
        // 逻辑过期通常不设置物理 TTL，或者设置一个远大于逻辑时间的 TTL（如 1 天）
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), 1, TimeUnit.DAYS);
        
    }
    private <T> void saveToCache(String key, T value) {
        saveToCache(key,value,30);
    }
}
