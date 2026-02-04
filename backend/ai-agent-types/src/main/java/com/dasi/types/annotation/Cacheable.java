package com.dasi.types.annotation;

import com.dasi.types.enumeration.CacheType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {

    String cacheKey() default "";

    String cachePrefix() default "";

    long cacheTtl() default 0;

    Class<?> cacheClass() default Object.class;

    CacheType cacheType() default CacheType.VALUE;

}
