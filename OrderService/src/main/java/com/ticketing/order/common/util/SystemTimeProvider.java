package com.ticketing.order.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Default implementation of TimeProvider using system time
 */
@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public LocalDateTime addMinutes(LocalDateTime dateTime, int minutes) {
        return dateTime.plusMinutes(minutes);
    }

    @Override
    public boolean isBeforeNow(LocalDateTime dateTime) {
        return dateTime.isBefore(now());
    }
}
