package com.dasi;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatTest {


    @Resource
    private OpenAiChatModel openAiChatModel;

    @Resource
    private OllamaChatModel ollamaChatModel;

    @Test
    public void testOpenai() {
        ChatResponse response = openAiChatModel.call(new Prompt(
                "1+1 = ?",
                OpenAiChatOptions.builder()
                        .model("Doubao-Seed-1.8")
                        .build()));

        String text = response.getResult().getOutput().getText();

        log.info("OpenAI 测试结果：{}", text);
    }

    @Test
    public void testOllama() {
        ChatResponse response = ollamaChatModel.call(new Prompt(
                "1+1 = ?",
                OpenAiChatOptions.builder()
                        .model("deepseek-r1:7b")
                        .build()));

        String text = response.getResult().getOutput().getText();

        log.info("Ollama 测试结果：{}", text);
    }


    @Value("classpath:dog.png")
    private org.springframework.core.io.Resource image;

    @Test
    public void testOllamaImage() {

        String userPrompt = "请描述这张图片的主要内容，并告诉我这是什么动物。";
        Media media = new Media(MimeType.valueOf(MimeTypeUtils.IMAGE_PNG_VALUE), image);

        UserMessage userMessage = new UserMessage(userPrompt, media);

        ChatResponse response = ollamaChatModel.call(new Prompt(
                userMessage,
                OpenAiChatOptions.builder()
                        .model("qwen3-vl:8b")
                        .build()));

        String text = response.getResult().getOutput().getText();

        log.info("图文测试结果：{}", text);
    }

    @Test
    public void testOpenaiImage() {

        String userPrompt = "请描述这张图片的主要内容，并告诉我这是什么动物。";
        Media media = new Media(MimeType.valueOf(MimeTypeUtils.IMAGE_PNG_VALUE), image);

        UserMessage userMessage = new UserMessage(userPrompt, media);

        ChatResponse response = openAiChatModel.call(new Prompt(
                userMessage,
                OpenAiChatOptions.builder()
                        .model("Doubao-Seed-1.8")
                        .build()));

        String text = response.getResult().getOutput().getText();

        log.info("图文测试结果：{}", text);
    }


}
