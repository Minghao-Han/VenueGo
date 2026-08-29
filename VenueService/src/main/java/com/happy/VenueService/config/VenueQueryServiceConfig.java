package com.happy.VenueService.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.happy.VenueService.service.VenueQueryService;
import com.happy.VenueService.service.impl.VenueCachedQueryServiceImpl;
import com.happy.VenueService.service.impl.VenueCaffeineQueryServiceImpl;

@Configuration
public class VenueQueryServiceConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "venue.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnProperty(prefix = "venue.cache", name = "level", havingValue = "L1", matchIfMissing = true)
    public VenueQueryService activeVenueQueryServiceL1(
            @Qualifier("rawVenueCachedQueryService") VenueCachedQueryServiceImpl rawCachedQueryService) {
        return rawCachedQueryService;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "venue.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnProperty(prefix = "venue.cache", name = "level", havingValue = "L2")
    public VenueQueryService activeVenueQueryServiceL2(VenueCaffeineQueryServiceImpl caffeineQueryService) {
        return caffeineQueryService;
    }
}