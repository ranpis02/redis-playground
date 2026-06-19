package com.review.Cache;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.review.model.dto.RedisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.review.utils.RedisConstants.*;

@Component
public class CacheClient {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    @Qualifier("unlockScript")
    private DefaultRedisScript<Long> unlockScript;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * Set the {@code value} and expiration {@code timeout} for {@code key} into Redis cache
     */
    public void set(String key, Object value, Long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), timeout, unit);
    }

    /**
     * Set the specified {@code value} for {@code key} into Redis cache with logical expiration
     * Caution: the key of logical expiration must be different from the key of normal cache, otherwise the logical expiration will not work
     */
    public void setWithLogicalExpire(String key, Object data, Long timeout, TimeUnit unit) {
        RedisDTO redisDTO = new RedisDTO();
        redisDTO.setData(data);
        redisDTO.setExpireTime(System.currentTimeMillis() + unit.toMillis(timeout));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisDTO));
    }

    /**
     * Query the cache by {@code id}. if the cache miss, fill the blank string to prevent cache penetration
     */
    public <ID, T> T queryWithBlankCache(String keyPrefix, ID id, Function<ID, T> dbFallback, Class<T> type, Long time, TimeUnit unit) {
        String key = keyPrefix + id;

        String resultJson = stringRedisTemplate.opsForValue().get(key);
        // If the cache hit, return the result
        if (resultJson != null && !resultJson.isBlank()) {
            return JSONUtil.toBean(resultJson, type);
        }

        // Return null, if the result is blank
        if (resultJson != null) {
            return null;
        }

        // Query the database, if the cache miss
        T dbResult = dbFallback.apply(id);
        if (dbResult == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_BLANK_TTL, TimeUnit.SECONDS
            );
            return null;
        }

        // Write the result into cache, if the data exists
        this.set(key, dbResult, time, unit);

        return dbResult;
    }

    /**
     * Query the cache by {@code id} with logical expiration. If the cache is expired, rebuild the cache asynchronously
     */
    public <T, ID> T queryWithLogicalExpiration(String keyPrefix, ID id, Function<ID, T> dbFallback, Class<T> type, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String resultJson = stringRedisTemplate.opsForValue().get(key);

        if (resultJson == null) return null;

        // RedisDTO<T> redisDTO = JSONUtil.toBean(resultJson, new TypeReference<RedisDTO<T>>() {
        // }, false);

        RedisDTO redisDTO = JSONUtil.toBean(resultJson, RedisDTO.class);

        // If the cache is not expired, return the data
        if (redisDTO.getExpireTime() > System.currentTimeMillis()) {
            return JSONUtil.toBean((JSONObject) redisDTO.getData(), type);
        }

        // If the cache is expired, then try to acquire the lock to rebuild the cache
        String lockKey = LOCK_KEY_PREFIX + id;
        String requestId = UUID.randomUUID() + ":" + Thread.currentThread().getId();

        boolean isBlock = tryLock(lockKey, requestId);

        if (isBlock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    T dbResult = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, dbResult, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unlock(lockKey, requestId);
                }
            });
        }

        return JSONUtil.toBean((JSONObject) redisDTO.getData(), type);
    }

    /**
     * Attempts to acquire a lock
     *
     * @param lockName  the key identifying the lock
     * @param requestId unique identifier for the lock owner, used to for safe release
     * @return true if the lock was acquired, false otherwise
     */
    private Boolean tryLock(String lockName, String requestId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue().
                        setIfAbsent(
                                lockName,
                                requestId,
                                LOCK_TTL,
                                TimeUnit.SECONDS
                        )
        );
    }

    /**
     * Release the distributed lock if owned by current thread
     *
     * @param key       the lock key
     * @param requestId unique identifier for the lock owner, used to verify ownership before releasing to prevent deleting locks held by others
     */
    private void unlock(String key, String requestId) {
        // String value = stringRedisTemplate.opsForValue().get(key);
        //
        // if (value != null && value.equals(requestId)) {
        //     stringRedisTemplate.delete(key);
        // }
        stringRedisTemplate.execute(
                unlockScript,
                Collections.singletonList(key),
                requestId
        );
    }
}
