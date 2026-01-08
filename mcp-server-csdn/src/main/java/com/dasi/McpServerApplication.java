package com.dasi;

import com.dasi.infrastructure.gateway.IPostCsdnService;
import com.dasi.mcp.tool.PostCsdnTool;
import com.dasi.type.properties.PostCsdnProperties;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    // 把 CSDN 的 HTTP 接口，变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public IPostCsdnService csdnService(PostCsdnProperties csdnProperties) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(csdnProperties.getBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(IPostCsdnService.class);
    }

    // 把工具类方法，注册成 MCP / Spring AI 能调用的 Tool
    @Bean
    public ToolCallbackProvider csdnTools(PostCsdnTool postCsdnTool) {
        return MethodToolCallbackProvider.builder().toolObjects(postCsdnTool).build();
    }

}
