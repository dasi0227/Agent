package com.dasi;

import com.dasi.sse.gateway.IPostCsdnHttp;
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
public class McpServerCsdnApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerCsdnApplication.class, args);
    }

    // 把 HTTP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public IPostCsdnHttp postCsdnHttp(PostCsdnProperties csdnProperties) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(csdnProperties.getBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(IPostCsdnHttp.class);
    }

    // 把 MCP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public ToolCallbackProvider csdnTools(PostCsdnTool postCsdnTool) {
        return MethodToolCallbackProvider.builder().toolObjects(postCsdnTool).build();
    }

}
