package com.happy.VenueService.UUID.Impl;
import com.happy.VenueService.UUID.IdGeneratorStrategy;
import org.springframework.stereotype.Component;
import java.util.UUID;


@Component("randomGenerator")
public class RandomIdGenerator implements IdGeneratorStrategy {
    @Override
    public UUID generateId() {
        return UUID.randomUUID();
    }

    @Override
    public String getStrategyName() {
        return "UUID_V4_RANDOM";
    }
}