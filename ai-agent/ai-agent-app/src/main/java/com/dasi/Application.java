package com.dasi;

import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
public class Application {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void flushRedisDbOnStartup() {
        if (redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
        }
    }

}
