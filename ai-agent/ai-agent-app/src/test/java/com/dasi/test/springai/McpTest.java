package com.dasi.test.springai;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class McpTest {

    @Resource
    private OpenAiChatModel chatModel;

    @Value(value = "${mcp.web-search.base-uri}")
    private String mcpWebSearchBaseUri;

    @Value(value = "${mcp.web-search.sse-endpoint}")
    private String mcpWebSearchSseEndpoint;

    @Value(value = "${mcp.web-search.api-key}")
    private String mcpWebSearchApiKey;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    public void testMcpBaiduWebSearch() {

        String sseEndpoint = mcpWebSearchSseEndpoint + "?api_key=" + mcpWebSearchApiKey;

        HttpClientSseClientTransport sseClient = HttpClientSseClientTransport
                .builder(mcpWebSearchBaseUri)
                .sseEndpoint(sseEndpoint)
                .build();

        McpSyncClient mcpSyncClient = McpClient
                .sync(sseClient)
                .initializationTimeout(Duration.ofSeconds(60))
                .requestTimeout(Duration.ofMinutes(3))
                .build();

        mcpSyncClient.initialize();

        ToolCallback[] toolCallbacks = new SyncMcpToolCallbackProvider(mcpSyncClient).getToolCallbacks();

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbacks)
                .build();

        String answer = chatClient.prompt()
                .user("你有什么工具可用，工具名和参数是什么")
                .call()
                .content();

        log.info("测试结果：{}", answer);
    }

    @Test
    public void testMcpElasticSearch() {

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();

        String answer = chatClient.prompt()
                .user("调用 list_indices 工具，返回索引列表（只输出结果）。")
                .call()
                .content();

        log.info("测试结果：{}", answer);

    }


    @Test
    public void testMcpGrafana() {

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();

        String answer = chatClient.prompt()
                .user("你可以告诉我 search_dashboards 必须提供的参数是什么？以及 get_dashboard_panel_queries 的返回内容是什么？")
                .call()
                .content();

        log.info("测试结果：{}", answer);

    }

}
