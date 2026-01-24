package com.dasi.type.util;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CacheUtil {

    private final String CACHE_NAME = "wecom";

    private final CacheManager cacheManager;

    public CacheUtil(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public <T> T getWithTtl(String key, Class<T> type) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return null;
        }
        CacheValue<?> cacheValue = cache.get(key, CacheValue.class);
        if (cacheValue == null) {
            return null;
        }
        if (cacheValue.isExpired()) {
            cache.evict(key);
            return null;
        }
        Object value = cacheValue.value();
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public void putWithTtl(Object key, Object value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return;
        }
        long expireAtEpochMs = System.currentTimeMillis() + ttl.toMillis();
        cache.put(key, new CacheValue<>(value, expireAtEpochMs));
    }

    private record CacheValue<T>(T value, long expireAtEpochMs) {
        boolean isExpired() {
            return System.currentTimeMillis() >= expireAtEpochMs;
        }
    }
}
