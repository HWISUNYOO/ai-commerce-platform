package com.aicommerce.product.config;

import com.aicommerce.product.web.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis 캐시 설정. "products" 캐시에는 {@link ProductResponse} 만 저장하므로
 * 타입 지정 JSON 직렬화기를 쓴다. Instant 등 Java 8 시간 타입 처리를 위해
 * JavaTimeModule 을 등록한 ObjectMapper 를 사용한다. TTL 10분.
 */
@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public RedisCacheConfiguration redisCacheConfiguration() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<ProductResponse> valueSerializer =
				new Jackson2JsonRedisSerializer<>(mapper, ProductResponse.class);

		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(10))
				.disableCachingNullValues()
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
	}
}
