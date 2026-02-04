package com.dasi.aop;

import com.dasi.domain.util.redis.IRedisService;
import com.dasi.types.enumeration.CacheType;
import com.dasi.types.annotation.Cacheable;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class CacheableAspect {

    @Resource
    private IRedisService redisService;

    @Around("@annotation(cacheable)")
    public Object around(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {

        String cacheKey = buildCacheKey(joinPoint.getArgs(), joinPoint.getSignature().getName(), cacheable.cacheKey(), cacheable.cachePrefix());

        CacheType cacheType = cacheable.cacheType();
        Class<?> cacheClass = cacheable.cacheClass();
        long cacheTtl = cacheable.cacheTtl();

        if (!cacheKey.isBlank() && cacheClass != Object.class) {
            Object cached = readCache(cacheKey, cacheType, cacheClass);
            if (cached != null) {
                return cached;
            }
        }

        Object cacheValue = joinPoint.proceed();
        if (cacheKey.isBlank()) {
            return cacheValue;
        }

        writeCache(cacheKey, cacheType, cacheValue, cacheTtl);
        return cacheValue;
    }

    private Object readCache(String cacheKey, CacheType type, Class<?> clazz) {
        return switch (type) {
            case VALUE -> redisService.getValue(cacheKey, clazz);
            case LIST -> redisService.getList(cacheKey, clazz);
            case SET -> redisService.getSet(cacheKey, clazz);
            case MAP -> redisService.getMap(cacheKey, clazz);
        };
    }

    private void writeCache(String cacheKey, CacheType cacheType, Object cacheValue, long cacheTtl) {
        if (cacheValue == null) {
            return;
        }

        switch (cacheType) {
            case VALUE -> {
                if (cacheTtl > 0) {
                    redisService.setValue(cacheKey, cacheValue, cacheTtl);
                } else {
                    redisService.setValue(cacheKey, cacheValue);
                }
            }
            case LIST -> {
                if (cacheValue instanceof List<?> list) {
                    if (cacheTtl > 0) {
                        redisService.setList(cacheKey, list, cacheTtl);
                    } else {
                        redisService.setList(cacheKey, list);
                    }
                }
            }
            case SET -> {
                if (cacheValue instanceof Set<?> set) {
                    if (cacheTtl > 0) {
                        redisService.addSet(cacheKey, set, cacheTtl);
                    } else {
                        redisService.addSet(cacheKey, set);
                    }
                }
            }
            case MAP -> {
                if (cacheValue instanceof Map<?, ?> map) {
                    Map<String, ?> value = (Map<String, ?>) map;
                    if (cacheTtl > 0) {
                        redisService.setMap(cacheKey, value, cacheTtl);
                    } else {
                        redisService.setMap(cacheKey, value);
                    }
                }
            }
        }
    }

    private String buildCacheKey(Object[] args, String methodName, String cacheKey, String cachePrefix) {
        if (StringUtils.isBlank(cacheKey) == StringUtils.isBlank(cachePrefix)) {
            throw new IllegalStateException("cacheKey 和 cachePrefix 必须有且只能有一个有值");
        }
        if (!StringUtils.isBlank(cacheKey)) {
            return cacheKey;
        }
        if (args == null || args.length == 0) {
            return cachePrefix + methodName;
        }
        return cachePrefix + Arrays.stream(args)
                .map(this::safeToString)
                .collect(Collectors.joining(","));
    }

    private String safeToString(Object arg) {
        if (arg == null) return "null";
        if (arg.getClass().isArray()) {
            if (arg instanceof Object[] objects) {
                return Arrays.deepToString(objects);
            }
            if (arg instanceof int[] ints) {
                return Arrays.toString(ints);
            }
            if (arg instanceof long[] longs) {
                return Arrays.toString(longs);
            }
            if (arg instanceof double[] doubles) {
                return Arrays.toString(doubles);
            }
            if (arg instanceof float[] floats) {
                return Arrays.toString(floats);
            }
            if (arg instanceof boolean[] booleans) {
                return Arrays.toString(booleans);
            }
            if (arg instanceof byte[] bytes) {
                return Arrays.toString(bytes);
            }
            if (arg instanceof char[] chars) {
                return Arrays.toString(chars);
            }
            if (arg instanceof short[] shorts) {
                return Arrays.toString(shorts);
            }
        }
        return String.valueOf(arg);
    }


}
