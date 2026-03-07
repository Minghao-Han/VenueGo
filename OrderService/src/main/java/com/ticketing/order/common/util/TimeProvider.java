package com.ticketing.order.common.util;

import java.time.LocalDateTime;

/**
 * Interface for time operations - allows injection of mock time provider for testing
 */
public interface TimeProvider {

    /**
     * Get current local date time
     */
    LocalDateTime now();

    /**
     * Get current system time in milliseconds
     */
    long currentTimeMillis();

    /**
     * Add minutes to a given time
     */
    LocalDateTime addMinutes(LocalDateTime dateTime, int minutes);

    /**
     * Check if a given time is before now
     */
    boolean isBeforeNow(LocalDateTime dateTime);
}
