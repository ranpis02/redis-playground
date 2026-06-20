package com.review.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Generate a globally unique, monotonically increasing ID in a distributed system.
 */
@Component
public class RedisIdWorker {
    // Choose a custom epoch tu reduce the length of generated IDs
    private static final long BEGIN_TIMESTAMP = 1781952107L;

    // Number of bits allocated for the sequence number
    private static final int COUNT_BITS = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // Generated a unique sequence number
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date); // For example, "icr:order:2026-06-01"

        if (count == null) {
            throw new IllegalStateException("Snowflake ID generator failed");
        }

        return (timestamp << COUNT_BITS) | count;
    }
}
