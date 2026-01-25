package com.dasi.config;

import com.dasi.properties.RedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JsonJacksonCodec codec = new JsonJacksonCodec(mapper);

        Config config = new Config();
        config.setCodec(codec);
        String address = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisProperties.getPassword())
                .setDatabase(redisProperties.getDatabase())
                .setConnectionPoolSize(redisProperties.getPoolSize())
                .setConnectionMinimumIdleSize(redisProperties.getMinIdleSize())
                .setIdleConnectionTimeout(redisProperties.getIdleTimeout())
                .setConnectTimeout(redisProperties.getConnectTimeout())
                .setRetryAttempts(redisProperties.getRetryAttempts())
                .setRetryInterval(redisProperties.getRetryInterval())
                .setPingConnectionInterval(redisProperties.getPingInterval())
                .setKeepAlive(redisProperties.isKeepAlive())
        ;

        return Redisson.create(config);
    }


}
