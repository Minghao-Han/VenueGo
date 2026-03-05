package com.happy.VenueService.util.Random;

import org.springframework.stereotype.Component;

@Component
public class JavaThreadRandomAdapter implements IRandom {
    @Override
    public long nextLong(long min, long max) {
        return java.util.concurrent.ThreadLocalRandom.current().nextLong(min, max);
    }
}