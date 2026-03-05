package com.happy.VenueService.util.Lock;

public interface ILock {
    /**
     * 尝试获取锁，立即返回结果
     * @return true if lock acquired, false otherwise
     */
    boolean tryLock(String key);

    /**
     * 释放锁
     */
    void unlock(String key);
}
