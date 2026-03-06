package com.happy.VenueService.util.UUID;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;


public class IdGeneratorProvider {
    
    private static IdGeneratorStrategy strategy;

    
    public static void setStrategy(IdGeneratorStrategy strategy) {
        IdGeneratorProvider.strategy = strategy;
    }

    public static UUID generateId() {
        if (strategy == null) {
            throw new IllegalStateException("IdGeneratorStrategy is not initialized. Check whether IdGeneratorConfig created the bean.");
        }
        return strategy.generateId();
    }
}