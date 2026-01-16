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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class McpWebSearchTest {

    @Resource
    private OpenAiChatModel chatModel;

    @Value(value = "${mcp.web-search.base-uri}")
    private String mcpWebSearchBaseUri;

    @Value(value = "${mcp.web-search.sse-endpoint}")
    private String mcpWebSearchSseEndpoint;

    @Value(value = "${mcp.web-search.api-key}")
    private String mcpWebSearchApiKey;

    @Test
    public void testMcpWebSearchBaidu() {

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

        try {
            mcpSyncClient.initialize();

            ToolCallback[] toolCallbacks = new SyncMcpToolCallbackProvider(mcpSyncClient).getToolCallbacks();

            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultToolCallbacks(toolCallbacks)
                    .build();

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String answer = chatClient.prompt()
                    .user("今天是 %s，搜索今日 AI 新闻，给出 2 条结果，包含标题和链接。".formatted(date))
                    .call()
                    .content();

            log.info("测试结果：{}", answer);
        } finally {
            mcpSyncClient.closeGracefully();
        }
    }

}
