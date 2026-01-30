package com.dasi.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datasource.postgresql", ignoreInvalidFields = true)
public class PostgreSQLProperties extends SQLDataSourceProperties {}
