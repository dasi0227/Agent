package com.dasi.infrastructure.redis;

import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class RedisService implements IRedisService {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public <T> void setValue(String key, T value) {
        redissonClient.<T>getBucket(key).set(value);
    }

    @Override
    public <T> T getValue(String key, Class<T> type) {
        Object value = redissonClient.getBucket(key).get();
        return value == null ? null : type.cast(value);
    }
}
