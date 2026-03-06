package com.happy.VenueService.util.Lock;

public interface ILock {
    /**
     * Try to acquire lock and return immediately.
     * @return true if lock acquired, false otherwise
     */
    boolean tryLock(String key);

    /**
     * Release lock.
     */
    void unlock(String key);
}
