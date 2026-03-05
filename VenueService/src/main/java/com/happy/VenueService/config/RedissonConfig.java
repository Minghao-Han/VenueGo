package com.happy.VenueService.config;

import java.util.List;
import java.util.UUID;

import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.happy.VenueService.repository.VenueRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;

@Configuration
@ConfigurationProperties(prefix = "bloom-filter.venue")
@Slf4j
@Data
public class RedissonConfig {

    private long expectedInsertions;
    private double falseProbability;
    private String name;
    private final String cacheValueKey = "cache:venue";
    private final String lockValueKey = "lock:venue";
    @Bean
    public RedissonClient redissonClient(DataRedisProperties redisProperties) {
        Config config = new Config();

        boolean sslEnabled = redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled();
        String prefix = sslEnabled ? "rediss://" : "redis://";

        // String username = redisProperties.getUsername();
        // String password = redisProperties.getPassword();

        if (redisProperties.getCluster() != null
                && redisProperties.getCluster().getNodes() != null
                && !redisProperties.getCluster().getNodes().isEmpty()) {

            var cluster = config.useClusterServers();
            for (String node : redisProperties.getCluster().getNodes()) {
                cluster.addNodeAddress(prefix + node);
            }
            // if (username != null && !username.isBlank()) cluster.setUsername(username);
            // if (password != null && !password.isBlank()) cluster.setPassword(password);

        } else {
            String address = prefix + redisProperties.getHost() + ":" + redisProperties.getPort();
            var single = config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(redisProperties.getDatabase());
            // if (username != null && !username.isBlank()) single.setUsername(username);
            // if (password != null && !password.isBlank()) single.setPassword(password);
        }

        return Redisson.create(config);
    }
    @Bean
    public RBloomFilter<String> venueBloomFilter(RedissonClient redissonClient, VenueRepository venueRepository) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(name);
        
        // 1. Initialize
        boolean isNew = bloomFilter.tryInit(expectedInsertions, falseProbability);
        
        if (isNew) {
            log.info("Bloom filter [{}] is new. Starting data pre-warm...", name);
            long startTime = System.currentTimeMillis();
            
            // 2. Fetch only IDs to save memory
            List<UUID> allVenueIds = venueRepository.findAllIds(); 
            
            if (allVenueIds != null && !allVenueIds.isEmpty()) {
                allVenueIds.forEach(id -> bloomFilter.add(cacheValueKey+":"+id.toString()));
                long duration = System.currentTimeMillis() - startTime;
                log.info("Pre-warm completed. Loaded {} IDs into bloom filter in {} ms.", allVenueIds.size(), duration);
                log.debug("an key example {}", cacheValueKey+":"+allVenueIds.get(0).toString());
            } else {
                log.warn("Pre-warm aborted: No venue IDs found in database.");
            }
        } else {
            log.info("Bloom filter [{}] already exists. Skipping pre-warm.", name);
        }
        
        return bloomFilter;
    }
}