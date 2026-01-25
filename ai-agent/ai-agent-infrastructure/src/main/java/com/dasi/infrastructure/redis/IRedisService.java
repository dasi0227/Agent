package com.dasi.infrastructure.redis;

public interface IRedisService {

    <T> void setValue(String key, T value);

    <T> T getValue(String key);

}
