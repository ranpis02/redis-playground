package com.review.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 配置类
 * 用于分布式锁等 Redisson 功能
 */
@Configuration
public class RedissonConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.database}")
    private int databaseNo;

    // If the password does exist, then set it to empty string
    @Value("${spring.data.redis.password:}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        String address = "redis://" + host + ":" + port;

        config.useSingleServer()
                .setAddress(address)
                .setDatabase(databaseNo);

        if (StringUtils.hasText(password)) {
            config.setPassword(password);
        }

        return Redisson.create(config);
    }
}
