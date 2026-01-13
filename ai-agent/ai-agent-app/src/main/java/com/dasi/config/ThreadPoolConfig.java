package com.dasi.config;

import com.dasi.properties.ThreadPoolProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfig {

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutor.class)
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolProperties properties) {
        log.info("【初始化配置】线程池：threadPoolExecutor");

        // 实例化策略
        RejectedExecutionHandler handler = switch (properties.getPolicy()) {
            case "DiscardPolicy" -> new ThreadPoolExecutor.DiscardPolicy();
            case "DiscardOldestPolicy" -> new ThreadPoolExecutor.DiscardOldestPolicy();
            case "CallerRunsPolicy" -> new ThreadPoolExecutor.CallerRunsPolicy();
            default -> new ThreadPoolExecutor.AbortPolicy();
        };

        // 创建线程池
        return new ThreadPoolExecutor(properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler);
    }

}
