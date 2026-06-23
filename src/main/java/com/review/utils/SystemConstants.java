package com.review.utils;

public class SystemConstants {
    /**
     * Prefix for Bearer tokens in the Authorization header
     */
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";

    /**
     * Prefix for auto-generated user nicknames
     */
    public static final String USER_NICK_NAME_PREFIX = "user_";

    /**
     * Timeout duration for shop queries in milliseconds
     */
    public static final Long SHOP_QUERY_TIMEOUT = 2000L;

    /**
     * Initial backoff duration for retrying failed operations in milliseconds
     */
    public static final int BACKOFF_INITIAL = 10;

    /**
     * Maximum number of items per page
     */
    public static final int MAX_PAGE_SIZE = 20;
}
