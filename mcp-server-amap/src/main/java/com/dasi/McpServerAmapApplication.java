package com.dasi;

import com.dasi.mcp.tool.AmapTool;
import com.dasi.sse.gateway.IAmapHttp;
import com.dasi.type.properties.AmapProperties;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@SpringBootApplication
public class McpServerAmapApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerAmapApplication.class, args);
    }

    // 把 HTTP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public IAmapHttp createHttp(AmapProperties AmapProperties) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AmapProperties.getBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(IAmapHttp.class);
    }

    // 把 MCP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public ToolCallbackProvider createTool(AmapTool amapTool) {
        return MethodToolCallbackProvider.builder().toolObjects(amapTool).build();
    }

}
