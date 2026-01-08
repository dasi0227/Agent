package com.dasi;

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
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.ai.mcp.client.enabled=false"
})
public class RagTest {

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    private OllamaChatModel ollamaChatModel;

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Test
    public void upload() {
        TikaDocumentReader reader = new TikaDocumentReader("./file.txt");

        List<Document> documents = reader.get();
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "wyw-info"));

        pgVectorStore.accept(documentSplitterList);

        log.info("上传完成");
    }

    @Test
    public void testOllamaRag() {
        String userMessage = "万驿苇是哪个大学的？万驿苇是哪里人？";
        String ragTag = "wyw-info";

        List<Message> messages = addRagMessage(userMessage, ragTag);

        ChatResponse response = ollamaChatModel.call(new Prompt(
                messages,
                OllamaOptions.builder()
                        .model("deepseek-r1:7b")
                        .build()));

        String text = response.getResult().getOutput().getText();

        log.info("测试结果：{}", text);
    }

    @Test
    public void testOpenaiRag() {
        String userMessage = "万驿苇是哪个大学的？万驿苇是哪里人？";
        String ragTag = "wyw-info";

        List<Message> messages = addRagMessage(userMessage, ragTag);

        ChatResponse response = openAiChatModel.call(new Prompt(
                messages,
                OllamaOptions.builder()
                        .model("Doubao-Seed-1.8")
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

}
