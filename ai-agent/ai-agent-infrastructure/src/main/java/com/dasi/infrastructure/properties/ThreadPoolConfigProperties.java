package com.dasi.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "thread.pool.executor.config", ignoreInvalidFields = true)
public class ThreadPoolConfigProperties {

    /** 核心线程数 */
    private Integer corePoolSize = 20;

    /** 最大线程数 */
    private Integer maxPoolSize = 200;

    /** 最大等待时间 */
    private Long keepAliveTime = 10L;

    /** 最大队列数 */
    private Integer blockQueueSize = 5000;

    /** 线程执行策略 */
    private String policy = "AbortPolicy";

}
