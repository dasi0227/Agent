package com.dasi.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OpenAiTest {

    @Resource
    private OpenAiChatModel chatModel;

    @Resource
    private OpenAiEmbeddingModel embeddingModel;

    @Resource
    private PgVectorStore pgVectorStore;

    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    @Test
    public void testChat() {
        ChatResponse response = chatModel.call(new Prompt(
                "1+1",
                OpenAiChatOptions.builder()
                        .model("glm-4.7")
                        .build()));
        String text = response.getResult().getOutput().getText();
        log.info("测试结果：{}", text);
    }

    @Test
    public void testEmbedding() {
        String input = "我是你爸爸";
        EmbeddingResponse response = embeddingModel.call(new EmbeddingRequest(
                List.of("我是你爸爸"),
                OpenAiEmbeddingOptions.builder()
                        .model("text-embedding-v4")
                        .build()));
        float[] vector = response.getResults().get(0).getOutput();
        log.info("测试结果：{}", vector);
    }


    @Value("classpath:info.txt")
    private org.springframework.core.io.Resource txtFile;

    @Value("classpath:dog.png")
    private org.springframework.core.io.Resource imgFile;

    @Test
    public void upload() {
        TikaDocumentReader reader = new TikaDocumentReader(txtFile);

        List<Document> documents = reader.get();
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "dasi-info"));

        pgVectorStore.accept(documentSplitterList);

        log.info("上传完成");
    }

    @Test
    public void testRag() {
        String userMessage = "Dasi 是什么大学的学生？Dasi 最喜欢吃什么？";
        String ragTag = "dasi-info";
        List<Message> messages = addRagMessage(userMessage, ragTag);

        ChatResponse response = chatModel.call(new Prompt(
                messages,
                OpenAiChatOptions.builder()
                        .model("deepseek-chat")
                        .build()));

        String text = response.getResult().getOutput().getText();
        log.info("测试结果：{}", text);
    }

    private List<Message> addRagMessage(String userMessage, String ragTag) {

        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                <DOCUMENTS>
                {documents}
                </DOCUMENTS>
                """;

        SearchRequest request = SearchRequest.builder()
                .query(userMessage)
                .topK(5)
                .filterExpression("knowledge == '" + ragTag + "'")
                .build();

        String systemPrompt = pgVectorStore.similaritySearch(request).stream().map(Document::getText).collect(Collectors.joining());

        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", systemPrompt));

        return List.of(ragMessage, new UserMessage(userMessage));
    }

    @Test
    public void testImg() {
        UserMessage userMessage = UserMessage.builder()
                .text("请判断图中是什么动物")
                .media(org.springframework.ai.content.Media.builder()
                        .mimeType(MimeType.valueOf(MimeTypeUtils.IMAGE_PNG_VALUE))
                        .data(imgFile)
                        .build())
                .build();

        ChatResponse response = chatModel.call(new Prompt(
                userMessage,
                OpenAiChatOptions.builder()
                        .model("doubao-Seed-1.8")
                        .build()));

        String text = response.getResult().getOutput().getText();
        log.info("测试结果：{}", text);
    }

}
