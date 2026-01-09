package com.dasi;

import com.dasi.mcp.tool.NoticeWeComTool;
import com.dasi.sse.gateway.INoticeWeComHttp;
import com.dasi.type.properties.NoticeWeComProperties;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@SpringBootApplication
public class McpServerWeComApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerWeComApplication.class, args);
    }

    // 把 HTTP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public INoticeWeComHttp noticeWeComHttp(NoticeWeComProperties noticeWeComProperties) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(noticeWeComProperties.getBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(INoticeWeComHttp.class);
    }

    // 把 MCP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public ToolCallbackProvider csdnTools(NoticeWeComTool noticeWeComTool) {
        return MethodToolCallbackProvider.builder().toolObjects(noticeWeComTool).build();
    }


}
