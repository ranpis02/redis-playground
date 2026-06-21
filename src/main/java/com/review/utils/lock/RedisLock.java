package com.review.utils.lock;

import java.util.concurrent.TimeUnit;

public interface RedisLock {
    /**
     * Try to acquire the lock.
     *
     * @param time TTL for the lock
     * @param unit Time unit for the TTL
     * @return true if the lock was acquired successfully, false otherwise
     */
    boolean tryLock(long time, TimeUnit unit);

    /**
     * Unlock operation
     */
    void unlock();
}
