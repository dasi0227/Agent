package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redis.sdk.config", ignoreInvalidFields = true)
public class RedisClientProperties {

    /** host:ip */
    private String host;

    /** 端口 */
    private Integer port;

    /** 数据库编号，默认为 0 */
    private Integer database = 0;

    /** 账密 */
    private String password;

    /** 设置连接池的大小，默认为64 */
    private Integer poolSize = 64;

    /** 设置连接池的最小空闲连接数，默认为10 */
    private Integer minIdleSize = 10;

    /** 设置连接的最大空闲时间（单位：毫秒），超过该时间的空闲连接将被关闭，默认为10000 */
    private Integer idleTimeout = 10000;

    /** 设置连接超时时间（单位：毫秒），默认为10000 */
    private Integer connectTimeout = 10000;

    /** 设置连接重试次数，默认为3 */
    private Integer retryAttempts = 3;

    /** 设置连接重试的间隔时间（单位：毫秒），默认为1000 */
    private Integer retryInterval = 1000;

    /** 设置定期检查连接是否可用的时间间隔（单位：毫秒），默认为0，表示不进行定期检查 */
    private Integer pingInterval = 0;

    /** 设置是否保持长连接，默认为true */
    private boolean keepAlive = true;

}
