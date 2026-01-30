package com.dasi.domain.util;

import java.util.List;
import java.util.Set;

public interface IRedisService {

    void setValue(String key, Object value);

    <T> T getValue(String key, Class<T> type);

    void setList(String key, List<?> values);

    <T> List<T> getList(String key, Class<T> elementType);

    void addSetMembers(String key, Set<?> values);

    <T> Set<T> getSetMembers(String key, Class<T> elementType);

    void delete(String key);

    void clear();

}
