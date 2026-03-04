package com.happy.VenueService.UUID;
import java.util.UUID;

public class IdGeneratorProvider {
    private static IdGeneratorStrategy strategy;

    public static void setStrategy(IdGeneratorStrategy strategy) {
        IdGeneratorProvider.strategy = strategy;
    }

    public static UUID generateId() {
        return strategy.generateId();
    }
}