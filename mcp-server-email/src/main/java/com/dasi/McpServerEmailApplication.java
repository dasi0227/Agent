package com.dasi;

import com.dasi.mcp.tool.EmailTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerEmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerEmailApplication.class, args);
    }

    // 把 MCP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public ToolCallbackProvider createTool(EmailTool emailTool) {
        return MethodToolCallbackProvider.builder().toolObjects(emailTool).build();
    }

}
