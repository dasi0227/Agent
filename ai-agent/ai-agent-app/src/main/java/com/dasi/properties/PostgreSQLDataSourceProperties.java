package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "datasource.postgresql", ignoreInvalidFields = true)
public class PostgreSQLDataSourceProperties {

    private String jdbcUrl;

    private String username;

    private String password;

    private String driverClassName;

    private String poolName;

}
