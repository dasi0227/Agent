package com.dasi.aop;

import com.dasi.domain.util.IRedisService;
import com.dasi.types.annotation.CacheEvict;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CacheEvictAspect {

    @Resource
    private IRedisService redisService;

    @AfterReturning("@annotation(cacheEvict)")
    public void afterReturn(CacheEvict cacheEvict) {

        String[] keyPrefixList = cacheEvict.keyPrefix();
        if (keyPrefixList == null) {
            return;
        }

        for (String keyPrefix : keyPrefixList) {
            redisService.deleteByPrefix(keyPrefix);
        }

    }

}
