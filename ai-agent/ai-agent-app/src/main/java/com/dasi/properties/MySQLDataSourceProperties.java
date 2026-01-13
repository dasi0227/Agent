package com.dasi.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datasource.mysql", ignoreInvalidFields = true)
public class MySQLDataSourceProperties extends SQLDataSourceProperties {}
