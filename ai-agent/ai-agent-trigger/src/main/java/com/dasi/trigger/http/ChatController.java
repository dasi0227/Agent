package com.dasi.trigger.http;

import com.dasi.api.IChatService;
import com.dasi.domain.chat.service.query.IQueryService;
import com.dasi.domain.chat.service.rag.IRagService;
import com.dasi.types.dto.request.ChatRequest;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;
import static com.dasi.types.common.MessageConstant.CHAT_ERROR_RESPONSE;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController implements IChatService {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private IQueryService queryService;

    @Resource
    private IRagService ragService;

    @PostMapping("/complete")
    @Override
    public String complete(@RequestBody ChatRequest chatRequest) {

        String clientId = chatRequest.getClientId();
        String userMessage = chatRequest.getUserMessage();
        String ragTag = chatRequest.getRagTag();

        log.info("【模型对话】完整对话：userMessage={}", userMessage);

        try {
            ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName(clientId), ChatClient.class);
            List<Message> messageList = ragService.addRagMessage(userMessage, ragTag);
            String response = chatClient.prompt()
                    .messages(messageList)
                    .call()
                    .content();
            if (response == null || response.isEmpty()) {
                return CHAT_ERROR_RESPONSE;
            }
            return response;
        } catch (Exception e) {
            log.error("【模型对话】完整对话失败：clientId={}", clientId, e);
            return CHAT_ERROR_RESPONSE;
        }
    }

    @PostMapping("/stream")
    @Override
    public Flux<String> stream(@RequestBody ChatRequest chatRequest) {

        String clientId = chatRequest.getClientId();
        String userMessage = chatRequest.getUserMessage();
        String ragTag = chatRequest.getRagTag();

        log.info("【模型对话】流式对话：userMessage={}", userMessage);

        try {
            ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName(clientId), ChatClient.class);
            List<Message> messageList = ragService.addRagMessage(userMessage, ragTag);
            return chatClient
                    .prompt()
                    .messages(messageList)
                    .stream()
                    .content()
                    .onErrorResume(e -> {
                        log.error("【模型对话】流式对话失败：clientId={}", clientId, e);
                        return Flux.just(CHAT_ERROR_RESPONSE);
                    });
        } catch (Exception e) {
            log.error("【模型对话】流式对话失败：clientId={}", clientId, e);
            return Flux.just(CHAT_ERROR_RESPONSE);
        }
    }

    @GetMapping("/chat-client-list")
    @Override
    public Result<List<ChatClientResponse>> queryChatClientResponseList() {
        List<ChatClientResponse> clientIdList = queryService.queryChatClientResponseList();
        return Result.success(clientIdList);
    }

    @GetMapping("/rag-tag-list")
    @Override
    public Result<List<String>> queryRagTagList() {
        List<String> ragTagList = queryService.queryRagTagList();
        return Result.success(ragTagList);
    }

}
