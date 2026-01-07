package com.dasi.config;

import com.dasi.properties.RedisClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisClientConfig {

    private final RedisClientProperties redisClientProperties;

    public RedisClientConfig(RedisClientProperties redisClientProperties) {
        this.redisClientProperties = redisClientProperties;
    }

    @Bean
    public RedissonClient redissonClient() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JsonJacksonCodec codec = new JsonJacksonCodec(mapper);

        Config config = new Config();
        config.setCodec(codec);
        String address = "redis://" + redisClientProperties.getHost() + ":" + redisClientProperties.getPort();
        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisClientProperties.getPassword())
                .setDatabase(redisClientProperties.getDatabase())
                .setConnectionPoolSize(redisClientProperties.getPoolSize())
                .setConnectionMinimumIdleSize(redisClientProperties.getMinIdleSize())
                .setIdleConnectionTimeout(redisClientProperties.getIdleTimeout())
                .setConnectTimeout(redisClientProperties.getConnectTimeout())
                .setRetryAttempts(redisClientProperties.getRetryAttempts())
                .setRetryInterval(redisClientProperties.getRetryInterval())
                .setPingConnectionInterval(redisClientProperties.getPingInterval())
                .setKeepAlive(redisClientProperties.isKeepAlive())
        ;

        return Redisson.create(config);
    }

}
