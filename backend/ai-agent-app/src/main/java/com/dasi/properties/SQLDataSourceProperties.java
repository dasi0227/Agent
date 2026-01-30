package com.dasi.properties;

import lombok.Data;

@Data
public class SQLDataSourceProperties {

    private String jdbcUrl;

    private String username;

    private String password;

    private String driverClassName;

    private String poolName;

}
