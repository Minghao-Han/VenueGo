package com.happy.VenueService.util.Lock.Impl;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.happy.VenueService.util.Lock.ILock;

@Component
public class RedissonRemoteLock implements ILock {
    private RedissonClient redissonClient;
    public RedissonRemoteLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    @Override
    public boolean tryLock(String key) {
        // 1. 获取锁对象（这只是一个标识，还没加锁）
        RLock lock = redissonClient.getLock(key);
        // 2. 尝试获取锁
        return lock.tryLock();
    }
    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        // isHeldByCurrentThread(): check if this lock is held by the current thread
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
    
}
