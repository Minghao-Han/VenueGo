package com.happy.CheckInService.util.UUID;
import java.util.UUID;

/**
 * Strategy interface for ID generation to decouple the entity from specific libraries.
 */
public interface IdGeneratorStrategy {
    
    /**
     * Generate a unique identifier as a UUID object.
     */
    UUID generateId();

    /**
     * Provide a name for the strategy (for logging/debugging).
     */
    String getStrategyName();
}