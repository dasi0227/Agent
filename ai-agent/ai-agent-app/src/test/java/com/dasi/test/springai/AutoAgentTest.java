package com.dasi.test.springai;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
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

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class AutoAgentTest {

    @Value(value = "${mcp.web-search.base-uri}")
    private String mcpWebSearchBaseUri;

    @Value(value = "${mcp.web-search.sse-endpoint}")
    private String mcpWebSearchSseEndpoint;

    @Value(value = "${mcp.web-search.api-key}")
    private String mcpWebSearchApiKey;

    @Resource
    private OpenAiChatModel chatModel;

    private ChatClient planningChatClient;

    private ChatClient executorChatClient;

    private ChatClient reviewerChatClient;

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";

    @Before
    public void init() {

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

        planningChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        角色：规划代理（Planner）
                        目标：把用户需求拆解为可执行的计划，保证可通过工具检索到当日信息。
                        当前日期：{current_date}
                        规则：
                        1) 仅输出计划，不执行任务。
                        2) 指定检索时间窗口为“今日”，列出关键查询词与信息源类型。
                        3) 计划步骤 3-5 个，要求可衡量、可验收。
                        4) 工具使用规范：必须通过 WebSearchMCP 检索。
                        输出格式（Markdown）：
                        - 目标概述
                        - 任务清单（每项含：目标/方法/工具/产出）
                        - 验收标准
                        """)
                .build();

        executorChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        角色：执行代理（Executor）
                        目标：产出“今日 AI 新闻总结”的最终稿。
                        当前日期：{current_date}
                        工具规则：
                        1) 获取最新信息必须调用 WebSearchMCP 工具，不允许编造来源。
                        2) 必须检索至少 10 条新闻来源，最终精选最重要的 3 条输出。
                        3) 工具不可用时，说明原因，并用已有知识提供替代性的低置信度总结。
                        输出要求（Markdown）：
                        - 按主题分组（如：大模型、产品发布、政策监管、投融资、开源社区）
                        - 每条新闻包含：标题、2-3 条要点、来源链接
                        - 只输出 3 条精选新闻，标注“今日”时间范围
                        """)
                .defaultToolCallbacks(toolCallbacks)
                .build();

        reviewerChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        角色：复核与润色代理（Reviewer）
                        目标：去重、纠错、补全并确保结构清晰。
                        当前日期：{current_date}
                        规则：
                        1) 保留每条新闻的来源链接，必要时可调用 WebSearchMCP 的 search 工具补充。
                        2) search 参数字段：
                           - query: string
                           - limit: number
                           - engines: string[] (bing/baidu/linuxdo/csdn/duckduckgo/exa/brave/juejin)
                        3) 删除重复或弱相关内容，保证覆盖面与可读性。
                        4) 只输出最终版本，不输出中间分析过程。
                        """)
                .defaultToolCallbacks(toolCallbacks)
                .build();
    }


    @Test
    public void test_agent_workflow() {
        String userRequest = "总结今日AI新闻";

        log.info("用户请求: {}", userRequest);

        // 第一步：任务规划 (Planning)
        log.info("--- 步骤1: 任务规划 ---");
        String planningPrompt = String.format("""
                请为以下用户需求制定详细的执行计划，并确保计划可以通过工具检索完成：
                用户需求：%s
                """, userRequest);

        String planningResult = planningChatClient
                .prompt(planningPrompt)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "workflow-planning-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .call().content();

        log.info("规划结果: {}", planningResult);

        // 第二步：任务执行 (Execution)
        log.info("--- 步骤2: 任务执行 ---");
        String executionContext = String.format("""
                根据以下任务规划，请逐步执行每个任务：

                任务规划：
                %s

                原始用户需求：%s

                输出要求：
                1) 必须使用 WebSearchMCP 工具检索“今日”AI新闻，至少 10 条来源链接。
                2) 从中精选 3 条最重要新闻输出。
                3) 每条新闻包含：标题、2-3 条要点、来源链接。
                4) 按主题分组，输出为可直接发布的 Markdown。
                """, planningResult, userRequest);

        String executionResult = executorChatClient
                .prompt(executionContext)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "workflow-execution-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call().content();

        log.info("执行结果: {}", executionResult);

        // 第三步：结果总结和验证
        log.info("--- 步骤3: 结果总结 ---");
        String summaryContext = String.format("""
                请对以下执行结果进行复核、补全和润色，确保满足原始需求：

                原始需求：%s

                执行结果：%s

                输出最终版本的 Markdown 文档。
                """, userRequest, executionResult);

        String summaryResult = reviewerChatClient
                .prompt(summaryContext)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "workflow-summary-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .call().content();

        log.info("总结报告: {}", summaryResult);
    }

}
