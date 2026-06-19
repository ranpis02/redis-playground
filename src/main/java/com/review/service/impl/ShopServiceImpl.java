package com.review.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.review.mapper.ShopMapper;
import com.review.model.dto.RedisDTO;
import com.review.model.entity.Shop;
import com.review.service.ShopService;
import com.review.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.review.utils.RedisConstants.*;
import static com.review.utils.SystemConstants.*;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    private static final long EXPIRATION_TIME = 20L;

    @Override
    public R queryById(Long id) {
        // warm-up the data
        saveShopWithLogicalExpire(10L,  EXPIRATION_TIME);
        saveShopWithLogicalExpire(9L,  EXPIRATION_TIME);

        Shop shop = queryWithLogicExpiration(id);

        if (shop != null) {
            return R.ok("Query shop successfully");
        }

        return R.fail("Shop does not exist");

    }

    /**
     * Query the shop data with mutex lock to prevent cache penetration
     *
     * @return the target value
     */
    public Shop queryWithMutex(Long id) {
        String cacheKey = CACHE_SHOP_KEY_PREFIX + id;

        // Try to get the shop data from Redis cache
        String shopJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (shopJson != null && !shopJson.isBlank()) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // Check the target value whether is blank, prevent cache penetration
        if (shopJson != null) {
            return null;
        }

        // Cache miss, then rebuild the cache
        String lockKey = LOCK_SHOP_KEY_PREFIX + id;
        String requestId = UUID.randomUUID() + ":" + Thread.currentThread().getId();
        // Try to acquire the lock
        // boolean isBlock = tryLock(lockKey, requestId);
        // // Fail to acquire the lock, then wait and retry
        // if (!isBlock) {
        //     try {
        //         Thread.sleep(50);
        //     } catch (InterruptedException e) {
        //         throw new RuntimeException(e);
        //     }
        //     return queryWithMutex(id);
        // }
        long start = System.currentTimeMillis();
        int backoff = BACKOFF_INITIAL;

        // Spin lock + exponential back off (prevent thunder herd promblem)
        while(!tryLock(lockKey, requestId)) {
            if(System.currentTimeMillis() - start > SHOP_QUERY_TIMEOUT) {
                throw new RuntimeException("Failed to acquire lock after " + SHOP_QUERY_TIMEOUT + "ms");
            }

            try {
                Thread.sleep(backoff + ThreadLocalRandom.current().nextInt(10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            backoff = Math.min(backoff * 2, 200);
        }

        try {
            // Double-check the cache after acquiring the lock
            shopJson = stringRedisTemplate.opsForValue().get(cacheKey);
            if (shopJson != null && !shopJson.isBlank()) {
                return JSONUtil.toBean(shopJson, Shop.class);
            }

            // Query the database
            Shop shop = getById(id);

            // Mock the delay of the rebuild work
            Thread.sleep(200);

            // Check whether the target value exists
            if (shop == null) {
                // Even if the target value does not exist, we still cache a blank value to prevent cache penetration
                stringRedisTemplate.opsForValue().set(cacheKey, "", CACHE_SHOP_TTL, TimeUnit.SECONDS);
                return null;
            }

            // If the user exists, then write the data into redis
            String shopJsonStr = JSONUtil.toJsonStr(shop);
            stringRedisTemplate.opsForValue().set(cacheKey, shopJsonStr, CACHE_SHOP_TTL, TimeUnit.SECONDS);
            return shop;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // Release the lock
            unlock(lockKey, requestId);
        }
    }

    /**
     * Saves the shop data into Redis cache with logical expiration to prevent cache breakdown (for cache warm-up)
     *
     * @param id            the shop id
     * @param expireSeconds the TTL for the cache entry in seconds
     */
    public void saveShopWithLogicalExpire(Long id, Long expireSeconds) {
        Shop shop = getById(id);
        try {
            Thread.sleep(100); // Mock the delay of the rebuild work
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        RedisDTO<Shop> redisDTO = new RedisDTO<>();
        redisDTO.setData(shop);
        redisDTO.setExpireTime(System.currentTimeMillis() + expireSeconds * 1000);

        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY_PREFIX + id, JSONUtil.toJsonStr(redisDTO));
    }

    /**
     * Query the shop data with logical expiration to prevent cache breakdown
     *
     * @param id the shop id
     * @return the shop data, or null if not found
     */
    public Shop queryWithLogicExpiration(Long id) {
        String cacheKey = CACHE_SHOP_KEY_PREFIX + id;
        String redisDTOJson = stringRedisTemplate.opsForValue().get(cacheKey);

        // If cache is not hit, then query from the database and rebuild the cache (indicate data is not preheating)
        if (redisDTOJson == null || redisDTOJson.isBlank()) {
            return null;
        }

        // Cache hit, and then parse the redisDTOJson
        // RedisDTO redisDTO = JSONUtil.toBean(redisDTOJson, RedisDTO.class);
        RedisDTO<Shop> redisDTO = JSONUtil.toBean(redisDTOJson, new TypeReference<RedisDTO<Shop>>() {
        }, false);
        Shop shopData = redisDTO.getData();

        if (redisDTO.getExpireTime() > System.currentTimeMillis()) {
            return shopData;
        }

        // Cache is expired, then try to rebuild the cache
        String lockKey = LOCK_SHOP_KEY_PREFIX + id;
        String requestId = UUID.randomUUID() + ":" + Thread.currentThread().getId();

        boolean isBlock = tryLock(lockKey, requestId);

        if(isBlock) {
            // Double-check the cache before rebuilding cache
            String latestRedisDTOJson = stringRedisTemplate.opsForValue().get(cacheKey);

            if(latestRedisDTOJson != null && !latestRedisDTOJson.isBlank()) {
                RedisDTO<Shop> latestRedisDTO = JSONUtil.toBean(latestRedisDTOJson, new TypeReference<RedisDTO<Shop>>() {}, false);

                if(latestRedisDTO.getExpireTime() > System.currentTimeMillis()) {
                    return latestRedisDTO.getData();
                }
            }

            // Start a separate thread for asynchronous rebuild
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try{
                    saveShopWithLogicalExpire(id, EXPIRATION_TIME);
                } catch(Exception e) {
                    log.error("Error rebuilding cache for shop id " + id, e);
                } finally {
                    unlock(lockKey, requestId);
                }
            });
        }

        return shopData;
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
                                CACHE_SHOP_TTL,
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
        String value = stringRedisTemplate.opsForValue().get(key);

        if (value != null && value.equals(requestId)) {
            stringRedisTemplate.delete(key);
        }
    }
}
