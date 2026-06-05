package com.tierpeek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

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
        // Redis 직렬화용 ObjectMapper (API용과 분리)
        ObjectMapper redisMapper = new ObjectMapper();
        redisMapper.registerModule(new JavaTimeModule());
        redisMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.tierpeek.dto.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.time.")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(redisMapper)));

        RedisCacheConfiguration defaultConfig = base
                .entryTtl(Duration.ofMinutes(30));

        RedisCacheConfiguration dashboardConfig = base
                .entryTtl(Duration.ofMinutes(5));

        RedisCacheConfiguration rankHistoryConfig = base
                .entryTtl(Duration.ofMinutes(30));

        RedisCacheConfiguration matchesConfig = base
                .entryTtl(Duration.ofHours(1));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(DASHBOARD_CACHE, dashboardConfig)
                .withCacheConfiguration(RANK_HISTORY_CACHE, rankHistoryConfig)
                .withCacheConfiguration(MATCHES_CACHE, matchesConfig)
                .build();
    }
}

