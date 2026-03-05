package com.happy.CheckInService.util.UUID.Impl;
import com.happy.CheckInService.util.UUID.IdGeneratorStrategy;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
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