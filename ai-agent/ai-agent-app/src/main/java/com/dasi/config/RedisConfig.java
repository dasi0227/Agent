package com.dasi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {

        log.info("【初始化配置】RedisTemplate");

        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        StringRedisSerializer valueSerializer = new StringRedisSerializer();

        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
