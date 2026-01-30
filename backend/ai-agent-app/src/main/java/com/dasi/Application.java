package com.dasi;

import com.dasi.domain.util.IRedisService;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class Application {

    @Resource
    private IRedisService redisService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void flushRedisDbOnStartup() {
        redisService.clear();
    }

}
