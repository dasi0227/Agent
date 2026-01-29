package com.dasi.infrastructure.redis;

import com.dasi.domain.util.IRedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@SuppressWarnings("unchecked")
@Slf4j
@Service
public class RedisService implements IRedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public <T> void setStringValue(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public <T> T getStringValue(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public <T> Set<T> getSetValue(String key) {
        return (Set<T>) redisTemplate.opsForSet().members(key);
    }

    @Override
    public <T> void addSetValue(String key, Set<T> value) {
        if (value == null || value.isEmpty()) return;
        T[] arr = (T[]) value.toArray();
        redisTemplate.opsForSet().add(key, arr);
    }

    @Override
    public <T> void resetSetValue(String key, Set<T> value) {
        redisTemplate.delete(key);
        addSetValue(key, value);
    }

}
