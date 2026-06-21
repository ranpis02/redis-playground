package com.review.utils.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.review.utils.RedisConstants.*;

public class SimpleRedisLock implements RedisLock {

    private final String name;

    private final StringRedisTemplate stringRedisTemplate;

    private final DefaultRedisScript<Long> unlockScript;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate,  DefaultRedisScript<Long> unlockScript) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
        this.unlockScript = unlockScript;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        String threadId = THREAD_ID_PREFIX + Thread.currentThread().getId();

        return Boolean.TRUE.equals(stringRedisTemplate
                .opsForValue()
                .setIfAbsent(LOCK_KEY_PREFIX + name, threadId, time, unit));
    }

    @Override
    public void unlock() {
        stringRedisTemplate.execute(
                unlockScript,
                Collections.singletonList(LOCK_KEY_PREFIX + name),
                THREAD_ID_PREFIX + Thread.currentThread().getId()
        );
    }
}
