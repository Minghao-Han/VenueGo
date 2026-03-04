package com.happy.VenueService.UUID.Impl;

import com.happy.VenueService.UUID.IdGeneratorStrategy;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("timeOrderedGenerator")
public class TimeOrderedIdGenerator implements IdGeneratorStrategy {
    @Override
    public UUID generateId() {
        // v7 is optimized for DB indexing (B-Tree friendly)
        return UuidCreator.getTimeOrderedEpoch();
    }

    @Override
    public String getStrategyName() {
        return "UUID_V7_TIME_ORDERED";
    }
}