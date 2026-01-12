package com.dasi.test;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AgentTest {

    @Resource
    private ChatClient.Builder chatClientBuilder;

    private ChatClient chatClient;

    @Resource
    private PgVectorStore pgVectorStore;

    @Before
    public void setUp() {

        McpSyncClient stdio = stdioMcpClient();
        McpSyncClient csdn = sseMcpClient1();
        McpSyncClient wecom = sseMcpClient2();

        chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("glm-4.7")
                        .build())
                .defaultSystem("""
                        你是一个 AI Agent 智能体，可以根据用户输入信息生成文章，并发送到 CSDN 平台以及完成微信公众号消息通知
                        你擅长使用Planning模式，帮助用户生成质量更高的文章。
                        你的规划应该包括以下几个方面：
                        1. 分析用户输入的内容，生成技术文章。
                        2. 提取文章标题、文章内容、文章简述，将以上内容发布文章到 CSDN
                        3. 获取发送到 CSDN 文章的 URL 地址
                        4. 企业微信应用消息通知，主题：为文章标题、描述：为文章简述、跳转地址：从发布文章到CSDN获取 URL 地址
                        """)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(stdio, csdn, wecom).getToolCallbacks())
//                .defaultAdvisors(
//                        PromptChatMemoryAdvisor.builder(
//                                MessageWindowChatMemory.builder().maxMessages(100).build()
//                        ).build(),
//                        new RagAnswerAdvisor(pgVectorStore, SearchRequest.builder()
//                                .topK(5)
//                                .filterExpression("knowledge == 'dasi-info'")
//                                .build()),
//                        SimpleLoggerAdvisor.builder().build()
//                )
                .build();
    }


    public McpSyncClient stdioMcpClient() {
        ServerParameters stdioParams = ServerParameters.builder("npx")
                .args("-y", "@modelcontextprotocol/server-filesystem", "/Users/wyw/Downloads", "/Users/wyw/Downloads")
                .build();
        McpSyncClient mcpSyncClient = McpClient.sync(new StdioClientTransport(stdioParams)).requestTimeout(Duration.ofSeconds(10)).build();
        mcpSyncClient.initialize();
        return mcpSyncClient;
    }

    public McpSyncClient sseMcpClient1() {
        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport.builder("http://127.0.0.1:9001").build();
        McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(180)).build();
        mcpSyncClient.initialize();
        return mcpSyncClient;
    }

    public McpSyncClient sseMcpClient2() {
        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport.builder("http://127.0.0.1:9002").build();
        McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(180)).build();
        mcpSyncClient.initialize();
        return mcpSyncClient;
    }

    @Test
    public void testShowTools() {
        String result = chatClient.prompt()
                .user("请使用清晰的列表结构列出你当前可以使用的所有 MCP 工具")
                .call()
                .content();

        log.info("=== MCP TOOLS ===\n{}", result);
    }

    @Test
    public void testUseTools() {
        String userMessage = """
            你必须严格按顺序完成以下 3 个步骤，并且每一步都必须通过工具调用完成：
            
            步骤 1：发布文章到 CSDN
            - 调用工具：saveArticle
            - title：Java 趣闻
            - markdowncontent：请生成一篇不超过 500 字的 Java 趣味小故事，风格幽默、有技术内涵，受众人群不是小白，而是已经入门的后端程序员
            
            步骤 2：发送企业微信应用消息
            - 调用工具：noticeArticle
            - title：Java 趣闻
            - description：基于步骤 1 的文章内容生成摘要，不超过 128 字
            - url：使用步骤 1 返回的文章 url
            
            步骤 3：保存结果到本地文件
            - 调用工具：writeFile
            - path：~/Downloads/{timestamp}.txt
            - content：基于生成的文章内容和上面两步返回的数据写入，格式如下
            ```
            csdn: {"code":"", "msg":"", "articleId":"", "url":"", "qrcode":""},
            wecom: {"code":"", "info":"", "msgid":""}
            文章标题：
            文章内容：
            ```
            输出要求：
            - 全部成功：仅输出“成功”
            - 任一步失败：仅输出失败原因
            - 不要输出多余解释或过程说明
            """;

        String result = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        log.info("调用结果：{}", result);
    }

}
