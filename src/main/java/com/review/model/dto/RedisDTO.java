package com.review.model.dto;

import lombok.Data;

@Data
public class RedisDTO {
    /**
     * The data to be stored into cache
     */
    private Object data;

    /**
     * TTL for the cache entry
     */
    private Long expireTime;
}
