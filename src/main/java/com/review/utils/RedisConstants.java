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
     * Login user info TTL in seconds
     */
    public static final Long LOGIN_USER_TTL = 36000L;
}
