package com.dasi.config;

import com.dasi.properties.HikariDataSourceProperties;
import com.dasi.properties.MySQLDataSourceProperties;
import com.dasi.properties.PostgreSQLDataSourceProperties;
import com.dasi.properties.SQLDataSourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableConfigurationProperties({MySQLDataSourceProperties.class, PostgreSQLDataSourceProperties.class, HikariDataSourceProperties.class})
public class DataSourceConfig {

    /**
     * MySQL DataSource
     */
    @Bean(name = "mysqlDataSource")
    @Primary
    public DataSource mysqlDataSource(MySQLDataSourceProperties mysqlProps, HikariDataSourceProperties hikariProps) {
        HikariConfig hikariConfig = buildHikariConfig(mysqlProps, hikariProps);
        return new HikariDataSource(hikariConfig);
    }

    /**
     * PostgreSQL DataSource
     */
    @Bean(name = "postgresqlDataSource")
    public DataSource postgresqlDataSource(PostgreSQLDataSourceProperties postgresqlProps, HikariDataSourceProperties hikariProps) {
        HikariConfig hikariConfig = buildHikariConfig(postgresqlProps, hikariProps);
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 合并单独配置和全局配置
     */
    private HikariConfig buildHikariConfig(SQLDataSourceProperties sqlProps, HikariDataSourceProperties hikariProps) {
        HikariConfig hikariConfig = new HikariConfig();

        // 1) 先填各自数据源的必填项
        hikariConfig.setJdbcUrl(sqlProps.getJdbcUrl());
        hikariConfig.setUsername(sqlProps.getUsername());
        hikariConfig.setPassword(sqlProps.getPassword());
        hikariConfig.setDriverClassName(sqlProps.getDriverClassName());
        hikariConfig.setPoolName(sqlProps.getPoolName());

        // 2) 再套全局 Hikari 配置
        hikariConfig.setMinimumIdle(hikariProps.getMinimumIdle());
        hikariConfig.setMaximumPoolSize(hikariProps.getMaximumPoolSize());
        hikariConfig.setIdleTimeout(hikariProps.getIdleTimeout());
        hikariConfig.setMaxLifetime(hikariProps.getMaxLifetime());
        hikariConfig.setConnectionTimeout(hikariProps.getConnectionTimeout());
        hikariConfig.setConnectionTestQuery(hikariProps.getConnectionTestQuery());
        hikariConfig.setInitializationFailTimeout(hikariProps.getInitializationFailTimeout());

        return hikariConfig;
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mysqlDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setConfigLocation(resolver.getResource("classpath:mybatis-config.xml"));
        Resource[] mappers = resolver.getResources("classpath*:mapper/**/*.xml");
        if (mappers.length > 0) {
            factoryBean.setMapperLocations(mappers);
        }

        return factoryBean.getObject();
    }

    @Bean(name = "mysqlTemplate")
    public SqlSessionTemplate mysqlTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        log.info("【初始化配置】MySQL 操作模版：SqlSessionTemplate");
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "postgresqlTemplate")
    public JdbcTemplate postgresqlTemplate(@Qualifier("postgresqlDataSource") DataSource dataSource) {
        log.info("【初始化配置】PostgreSQL 操作模版：JdbcTemplate");
        return new JdbcTemplate(dataSource);
    }

}
