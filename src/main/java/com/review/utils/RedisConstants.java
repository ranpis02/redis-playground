package com.review.utils;

public class RedisConstants {
    /**
     * Prefix for login verification code keys
     */
    public static final String LOGIN_CODE_PREFIX = "login:code:";

    /**
     * Login verification code TTL in seconds
     */
    public static final Long LOGIN_CODE_TTL = 5L;
    /**
     * Prefix for login user info keys
     */
    public static final String LOGIN_USER_PREFIX = "login:user:";

    /**
     * Login user info TTL in milliseconds
     */
    public static final Long LOGIN_USER_TTL = 36000L;

    /**
     * Prefix for shop cache keys
     */
    public static final String CACHE_SHOP_KEY_PREFIX = "cache:shop:";

    /**
     * Shop cache TTL in seconds
     */
    public static final Long CACHE_SHOP_TTL = 10L;

    /**
     * Blank cache TTL in seconds
     */
    public static final Long CACHE_BLANK_TTL = 10L;

    /**
     * Prefix for lock shop keys
     */
    public static final String LOCK_SHOP_KEY_PREFIX = "lock:shop:";

    /**
     * General prefix for lock keys
     */
    public static final String LOCK_KEY_PREFIX = "lock:";

    /**
     * Lock TTL in seconds
     */
    public static final Long LOCK_TTL = 10L;
}
