package com.dasi.infrastructure.util;

import com.dasi.domain.util.redis.IRedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class RedisService implements IRedisService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void setValue(String key, Object value) {
        if (key == null || key.isBlank()) return;
        if (value == null) {
            redisTemplate.delete(key);
            return;
        }
        try {
            String json = (value instanceof String) ? (String) value : objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            log.error("【Redis】序列化失败：key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public void setValue(String key, Object value, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            setValue(key, value);
            return;
        }
        if (key == null || key.isBlank()) return;
        if (value == null) {
            redisTemplate.delete(key);
            return;
        }
        try {
            String json = (value instanceof String) ? (String) value : objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, java.time.Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException e) {
            log.error("【Redis】序列化失败：key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> T getValue(String key, Class<T> type) {
        if (key == null || key.isBlank() || type == null) return null;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        if (type == String.class) {
            return type.cast(json);
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("【Redis】反序列化失败：key={}, type={}, error={}", key, type.getName(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void setList(String key, List<?> values) {
        if (key == null || key.isBlank()) return;
        if (values == null) {
            redisTemplate.delete(key);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(values);
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            log.error("【Redis】List 序列化失败：key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public void setList(String key, List<?> values, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            setList(key, values);
            return;
        }
        if (key == null || key.isBlank()) return;
        if (values == null) {
            redisTemplate.delete(key);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(values);
            redisTemplate.opsForValue().set(key, json, java.time.Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException e) {
            log.error("【Redis】List 序列化失败：key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> List<T> getList(String key, Class<T> elementType) {
        if (key == null || key.isBlank() || elementType == null) return List.of();
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        if (json.isBlank()) return List.of();
        try {
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            List<T> list = objectMapper.readValue(json, type);
            return list == null ? List.of() : list;
        } catch (Exception e) {
            log.error("【Redis】List 反序列化失败：key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void addSet(String key, Set<?> values) {
        if (key == null || key.isBlank() || values == null || values.isEmpty()) return;
        Set<String> jsonSet = new HashSet<>();
        for (Object value : values) {
            if (value == null) continue;
            if (value instanceof String stringValue) {
                jsonSet.add(stringValue);
                continue;
            }
            try {
                jsonSet.add(objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                log.error("【Redis】Set 序列化失败：key={}, error={}", key, e.getMessage(), e);
            }
        }
        if (!jsonSet.isEmpty()) {
            redisTemplate.opsForSet().add(key, jsonSet.toArray(new String[0]));
        }
    }

    @Override
    public void addSet(String key, Set<?> values, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            addSet(key, values);
            return;
        }
        if (key == null || key.isBlank() || values == null || values.isEmpty()) return;
        Set<String> jsonSet = new HashSet<>();
        for (Object value : values) {
            if (value == null) continue;
            if (value instanceof String stringValue) {
                jsonSet.add(stringValue);
                continue;
            }
            try {
                jsonSet.add(objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                log.error("【Redis】Set 序列化失败：key={}, error={}", key, e.getMessage(), e);
            }
        }
        if (!jsonSet.isEmpty()) {
            redisTemplate.opsForSet().add(key, jsonSet.toArray(new String[0]));
            redisTemplate.expire(key, java.time.Duration.ofSeconds(ttlSeconds));
        }
    }

    @Override
    public <T> Set<T> getSet(String key, Class<T> elementType) {
        if (key == null || key.isBlank() || elementType == null) return null;
        Set<String> jsonSet = redisTemplate.opsForSet().members(key);
        if (jsonSet == null || jsonSet.isEmpty()) return null;

        Set<T> result = new HashSet<>();
        for (String json : jsonSet) {
            if (json == null) continue;
            if (elementType == String.class) {
                String value = json;
                if (value.startsWith("\"")) {
                    try {
                        value = objectMapper.readValue(value, String.class);
                    } catch (Exception e) {
                        log.error("【Redis】String 反序列化失败：key={}, error={}", key, e.getMessage(), e);
                    }
                }
                result.add(elementType.cast(value));
                continue;
            }
            try {
                result.add(objectMapper.readValue(json, elementType));
            } catch (Exception e) {
                log.error("【Redis】Set 反序列化失败：key={}, error={}", key, e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    public void setMap(String key, Map<String, ?> values) {
        if (key == null || key.isBlank()) return;
        if (values == null) {
            redisTemplate.delete(key);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(values);
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            log.error("【Redis】Map 序列化失败：key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public void setMap(String key, Map<String, ?> values, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            setMap(key, values);
            return;
        }
        if (key == null || key.isBlank()) return;
        if (values == null) {
            redisTemplate.delete(key);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(values);
            redisTemplate.opsForValue().set(key, json, java.time.Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException e) {
            log.error("【Redis】Map 序列化失败：key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> Map<String, T> getMap(String key, Class<T> valueType) {
        if (key == null || key.isBlank() || valueType == null) return Map.of();
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        if (json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, valueType)
            );
        } catch (Exception e) {
            log.error("【Redis】Map 反序列化失败：key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void deleteByKey(String key) {
        if (key == null || key.isBlank()) return;
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return;
        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(prefix + "*")
                    .count(500)
                    .build();
            try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options)) {
                while (cursor.hasNext()) {
                    byte[] key = cursor.next();
                    if (key != null && key.length > 0) {
                        redisTemplate.getConnectionFactory().getConnection().del(key);
                    }
                }
            }
        } catch (Exception e) {
            log.error("【Redis】按前缀删除失败：prefix={}, error={}", prefix, e.getMessage(), e);
        }
    }

    @Override
    public void clear() {
        assert redisTemplate.getConnectionFactory() != null;
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

}
