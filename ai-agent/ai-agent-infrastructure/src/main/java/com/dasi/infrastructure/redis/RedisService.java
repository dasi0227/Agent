package com.dasi.infrastructure.redis;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisService implements IRedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public <T> void setValue(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public <T> T getValue(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }
}
