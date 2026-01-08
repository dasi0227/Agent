package com.dasi.http;

import com.dasi.IChatService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.dasi.type.SystemConstant.SYSTEM_PROMPT;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController implements IChatService {

    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    @Qualifier("openaiChatClient")
    private ChatClient openAiChatClient;

    @Resource
    @Qualifier("ollamaChatClient")
    private ChatClient ollamaChatClient;

    private ChatClient resolveChatClient(String model) {
        return model.contains(":") ? ollamaChatClient : openAiChatClient;
    }

    private String resolveModel(String model) {
        return switch (model) {
            case "doubao-seed-1.8" -> "ep-20260107192819-vmhbf";
            default -> model;
        };
    }

    @GetMapping("/complete")
    @Override
    public ChatResponse complete(@RequestParam String model, @RequestParam String message, @RequestParam String ragTag) {
        model = resolveModel(model);
        ChatClient chatClient = resolveChatClient(model);

        if (ragTag == null || ragTag.isEmpty()) {
            ChatOptions chatOptions = ChatOptions.builder().model(model).build();
            return chatClient
                    .prompt()
                    .user(message)
                    .options(chatOptions)
                    .call()
                    .chatResponse();
        }

        List<Message> messageList = new ArrayList<>();
        ChatOptions chatOptions = addRag(message, model, ragTag, messageList);
        return chatClient
                .prompt()
                .messages(messageList)
                .options(chatOptions)
                .call()
                .chatResponse();
    }

    @GetMapping("/stream")
    @Override
    public Flux<ChatResponse> stream(@RequestParam String model, @RequestParam String message, @RequestParam String ragTag) {
        model = resolveModel(model);
        ChatClient chatClient = resolveChatClient(model);

        if (ragTag == null || ragTag.isEmpty()) {
            ChatOptions chatOptions = ChatOptions.builder().model(model).build();
            return chatClient
                    .prompt()
                    .user(message)
                    .options(chatOptions)
                    .stream()
                    .chatResponse();
        }

        List<Message> messageList = new ArrayList<>();
        ChatOptions chatOptions = addRag(message, model, ragTag, messageList);
        return chatClient
                .prompt()
                .messages(messageList)
                .options(chatOptions)
                .stream()
                .chatResponse();
    }

    private ChatOptions addRag(String message, String model, String ragTag, List<Message> messageList) {
        // 构造用户消息
        UserMessage userMessage = new UserMessage(message);

        // 构建向量检索条件
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        Filter.Expression expression = filterExpressionBuilder.eq("knowledge", ragTag).build();

        // 构建向量检索请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(message)
                .filterExpression(expression)
                .topK(5)
                .build();

        // 执行向量检索
        List<Document> documentList = pgVectorStore.similaritySearch(searchRequest);

        // 将检索结果合并为一个文本块，过滤空文档和空内容
        String documentCollectors = (documentList == null ? List.<Document>of() : documentList).stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        // 生成带占位符替换的系统消息
        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));

        // 组合系统消息与用户消息
        messageList.add(ragMessage);
        messageList.add(userMessage);

        // 构造对话选项
        return ChatOptions.builder().model(model).build();
    }

}
