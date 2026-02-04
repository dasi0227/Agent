package com.dasi.domain.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRedisService {

    void setValue(String key, Object value);
    void setValue(String key, Object value, long ttlSeconds);
    <T> T getValue(String key, Class<T> type);

    void setList(String key, List<?> values);
    void setList(String key, List<?> values, long ttlSeconds);
    <T> List<T> getList(String key, Class<T> elementType);

    void addSet(String key, Set<?> values);
    void addSet(String key, Set<?> values, long ttlSeconds);
    <T> Set<T> getSet(String key, Class<T> elementType);

    void setMap(String key, Map<String, ?> values, long ttlSeconds);
    void setMap(String key, Map<String, ?> values);
    <T> Map<String, T> getMap(String key, Class<T> valueType);

    void deleteByKey(String key);
    void deleteByPrefix(String prefix);

    void clear();

}
