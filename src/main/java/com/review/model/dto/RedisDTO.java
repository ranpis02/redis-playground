package com.review.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class RedisDTO<T> {
    /**
     * The data to be stored into cache
     */
    private T data;

    /**
     * TTL for the cache entry
     */
    private Long expireTime;
}
