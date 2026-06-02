package com.tierpeek.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * Redis 캐시 설정입니다.
 * 엔드포인트별로 다른 TTL을 적용합니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String DASHBOARD_CACHE = "dashboard";
    public static final String RANK_HISTORY_CACHE = "rankHistory";
    public static final String MATCHES_CACHE = "matches";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30));

        RedisCacheConfiguration dashboardConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5));

        RedisCacheConfiguration rankHistoryConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30));

        RedisCacheConfiguration matchesConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(DASHBOARD_CACHE, dashboardConfig)
                .withCacheConfiguration(RANK_HISTORY_CACHE, rankHistoryConfig)
                .withCacheConfiguration(MATCHES_CACHE, matchesConfig)
                .build();
    }
}

