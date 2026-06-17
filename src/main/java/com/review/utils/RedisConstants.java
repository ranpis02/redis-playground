package com.review.utils;

public class RedisConstants {
    /**
     * Login verification code key prefix
     */
    public static final String LOGIN_CODE_KEY = "login:code:";


    /**
     * Login user info key prefix
     */
    public static final String LOGIN_USER_KEY = "login:user:";

    /**
     * TTl of login user info in redis, unit is seconds
     */
    public static final Long LOGIN_USER_TTL = 36000L; // 10 Hours
}
