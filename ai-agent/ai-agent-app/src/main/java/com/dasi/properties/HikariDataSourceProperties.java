package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "datasource.hikari", ignoreInvalidFields = true)
public class HikariDataSourceProperties {

    private Integer minimumIdle;

    private Integer maximumPoolSize;

    private Long idleTimeout;

    private Long maxLifetime;

    private Long connectionTimeout;

    private String connectionTestQuery;

    private Long initializationFailTimeout;

}
