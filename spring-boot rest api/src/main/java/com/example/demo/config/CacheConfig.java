package com.example.demo.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
			@Value("${app.cache.products-ttl:10m}") Duration productsTtl) {
		GenericJacksonJsonRedisSerializer serializer = GenericJacksonJsonRedisSerializer.builder().build();
		RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(productsTtl)
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

		return RedisCacheManager.builder(redisConnectionFactory)
				.cacheDefaults(cacheConfiguration)
				.build();
	}

	@Bean
	CacheErrorHandler cacheErrorHandler() {
		return new CacheErrorHandler() {
			@Override
			public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
			}

			@Override
			public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
			}

			@Override
			public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
			}

			@Override
			public void handleCacheClearError(RuntimeException exception, Cache cache) {
			}
		};
	}
}
