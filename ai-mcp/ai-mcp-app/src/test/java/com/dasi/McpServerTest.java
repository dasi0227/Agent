package com.dasi;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class McpServerTest {

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Autowired
    private ToolCallbackProvider tools;

    @Test
    public void testMcpServer() {
        String userMessage = "1.获取当前电脑配置; 2.在 /Users/wyw/Downloads 文件夹下，创建 computer.txt; 3.把当前电脑配置写入 computer.txt";

        ChatClient chatClient = ChatClient
                .builder(openAiChatModel)
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("glm-4.7")
                        .build())
                .build();

        String content = chatClient.prompt(userMessage).call().content();

        log.info("测试结果：{}", content);
    }
}
