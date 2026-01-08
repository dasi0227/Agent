package com.dasi;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class McpTest {

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Resource
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private ToolCallbackProvider tools;

    @Test
    public void getTool() {
        String userMessage = "有哪些工具可以使用";

        ChatClient chatClient = ChatClient
                .builder(openAiChatModel)
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("Doubao-Seed-1.8")
                        .build())
                .build();

        String content = chatClient.prompt(userMessage).call().content();

        log.info("OpenAI 测试结果：{}", content);
    }

    @Test
    public void testOpenaiMcp1() {
        String userMessage = "1.在 /Users/wyw/Downloads 文件夹下，创建 temp.txt; 2.把你的模型简介写入 temp.txt";

        ChatClient chatClient = ChatClient
                .builder(openAiChatModel)
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("Doubao-Seed-1.8")
                        .build())
                .build();

        String content = chatClient.prompt(userMessage).call().content();

        log.info("测试结果：{}", content);
    }

    @Test
    public void testOpenaiMcp2() {
        String userMessage = "1.在 /Users/wyw/Downloads 文件夹下，创建 aa.txt; 2.把你的模型简介写入 aa.txt";

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
