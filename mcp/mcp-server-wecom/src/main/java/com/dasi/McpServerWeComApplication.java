package com.dasi;

import com.dasi.mcp.tool.WeComTool;
import com.dasi.sse.http.IWeComHttp;
import com.dasi.type.properties.WeComProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;

@SpringBootApplication
@EnableCaching
public class McpServerWeComApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerWeComApplication.class, args);
    }

    // 把 HTTP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public IWeComHttp createHttp(WeComProperties weComProperties) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(weComProperties.getBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(IWeComHttp.class);
    }

    // 把 MCP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public ToolCallbackProvider createTool(WeComTool weComTool) {
        return MethodToolCallbackProvider.builder().toolObjects(weComTool).build();
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("WeComAccessToken");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofHours(3)));
        return cacheManager;
    }

}
