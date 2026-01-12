package com.dasi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    /** Global Hikari 参数 */
    @Bean(name = "globalHikariConfig")
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig globalHikariConfig() {
        return new HikariConfig();
    }

    /** MySQL Hikari 参数 */
    @Bean(name = "mysqlHikariConfig")
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public HikariConfig mysqlHikariConfig() {
        return new HikariConfig();
    }

    /** PostgreSQL Hikari 参数 */
    @Bean(name = "postgresqlHikariConfig")
    @ConfigurationProperties(prefix = "spring.datasource.postgresql")
    public HikariConfig postgresqlHikariConfig() {
        return new HikariConfig();
    }

    /** MySQL DataSource */
    @Bean(name = "mysqlDataSource")
    @Primary
    public DataSource mysqlDataSource(
            @Qualifier("mysqlHikariConfig") HikariConfig mysqlConfig,
            @Qualifier("globalHikariConfig") HikariConfig globalConfig
    ) {
        applyGlobalHikari(globalConfig, mysqlConfig);
        return new HikariDataSource(mysqlConfig);
    }

    /** PostgreSQL DataSource */
    @Bean(name = "postgresqlDataSource")
    public DataSource postgresqlDataSource(
            @Qualifier("postgresqlHikariConfig") HikariConfig postgresqlConfig,
            @Qualifier("globalHikariConfig") HikariConfig globalConfig
    ) {
        applyGlobalHikari(globalConfig, postgresqlConfig);
        return new HikariDataSource(postgresqlConfig);
    }

    /** 合并单独配置和全局配置 */
    private void applyGlobalHikari(HikariConfig global, HikariConfig target) {
        if (global.getMinimumIdle() > 0) target.setMinimumIdle(global.getMinimumIdle());
        if (global.getMaximumPoolSize() > 0) target.setMaximumPoolSize(global.getMaximumPoolSize());
        if (global.getIdleTimeout() > 0) target.setIdleTimeout(global.getIdleTimeout());
        if (global.getMaxLifetime() > 0) target.setMaxLifetime(global.getMaxLifetime());
        if (global.getConnectionTimeout() > 0) target.setConnectionTimeout(global.getConnectionTimeout());

        String testQuery = global.getConnectionTestQuery();
        if (testQuery != null && !testQuery.isBlank()) {
            target.setConnectionTestQuery(testQuery);
        }
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(mysqlDataSource);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setConfigLocation(resolver.getResource("classpath:/mybatis/config/mybatis-config.xml"));
        Resource[] mappers = resolver.getResources("classpath*:/mybatis/mapper/**/*.xml");
        if (mappers.length > 0) {
            factoryBean.setMapperLocations(mappers);
        }

        return factoryBean.getObject();
    }

    @Bean(name = "mysqlTemplate")
    public SqlSessionTemplate mysqlTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "postgresqlTemplate")
    public JdbcTemplate postgresqlTemplate(@Qualifier("postgresqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
