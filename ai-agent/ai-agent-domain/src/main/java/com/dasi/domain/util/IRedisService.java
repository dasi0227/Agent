package com.dasi.domain.util;

import java.util.Set;

public interface IRedisService {

    <T> void setStringValue(String key, T value);

    <T> T getStringValue(String key);

    <T> Set<T> getSetValue(String key);

    <T> void addSetValue(String key, Set<T> value);

    <T> void resetSetValue(String key, Set<T> value);

}
