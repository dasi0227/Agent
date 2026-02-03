package com.dasi.trigger.http;

import com.dasi.api.IAiApi;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.service.augment.IAugmentService;
import com.dasi.domain.ai.service.dispatch.IDispatchService;
import com.dasi.domain.ai.service.rag.IRagService;
import com.dasi.domain.query.service.IQueryService;
import com.dasi.types.dto.response.query.QueryChatClientResponse;
import com.dasi.types.dto.response.query.QueryWorkAgentResponse;
import com.dasi.types.dto.request.ai.AiChatRequest;
import com.dasi.types.dto.request.ai.AiArmoryRequest;
import com.dasi.types.dto.request.ai.AiWorkRequest;
import com.dasi.types.dto.request.ai.AiUploadRequest;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;
import static com.dasi.types.constant.ChatConstant.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
public class AiController implements IAiApi {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private IDispatchService dispatchService;

    @Resource
    private IAugmentService augmentService;

    @Resource
    private IRagService ragService;

    @Resource
    private IQueryService queryService;

    @Override
    @PostMapping(value = "/work/execute", produces = "text/event-stream")
    public SseEmitter execute(@Valid @RequestBody AiWorkRequest aiWorkRequest) {

        SseEmitter sseEmitter = new SseEmitter(0L);
        String agentId = aiWorkRequest.getAiAgentId();
        if (!isActiveWorkAgent(agentId)) {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("Agent 未启用或不存在"));
            } catch (Exception e) {
                log.error("【Agent 执行】状态校验失败：agentId={}", agentId, e);
            } finally {
                sseEmitter.complete();
            }
            return sseEmitter;
        }

        ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                .agentId(agentId)
                .userMessage(aiWorkRequest.getUserMessage())
                .sessionId(aiWorkRequest.getSessionId())
                .maxRound(aiWorkRequest.getMaxRound())
                .maxRetry(aiWorkRequest.getMaxRetry())
                .build();

        dispatchService.dispatchExecuteStrategy(executeRequestEntity, sseEmitter);

        return sseEmitter;
    }

    @PostMapping("/chat/complete")
    @Override
    public String complete(@Valid @RequestBody AiChatRequest aiChatRequest) {

        String clientId = aiChatRequest.getClientId();
        if (isActiveChatClient(clientId)) {
            log.warn("【AI 对话】client 未启用或不存在：clientId={}", clientId);
            return CHAT_ERROR_RESPONSE;
        }

        String userMessage = aiChatRequest.getUserMessage();
        String ragTag = aiChatRequest.getRagTag();
        String sessionId = aiChatRequest.getSessionId();
        List<String> mcpIdList = aiChatRequest.getMcpIdList();
        Double temperature = aiChatRequest.getTemperature();
        Double presencePenalty = aiChatRequest.getPresencePenalty();
        Integer maxCompletionTokens = aiChatRequest.getMaxCompletionTokens();

        log.info("【AI 对话】完整对话开始：aiChatRequest={}", aiChatRequest);

        try {
            ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName(clientId), ChatClient.class);
            List<Message> messageList = augmentService.augmentRagMessage(userMessage, ragTag);
            SyncMcpToolCallbackProvider toolCallbackList = augmentService.augmentMcpTool(mcpIdList);
            ChatOptions chatOptions = OpenAiChatOptions.builder()
                    .temperature(temperature)
                    .presencePenalty(presencePenalty)
                    .maxCompletionTokens(maxCompletionTokens)
                    .build();

            String response = chatClient.prompt()
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE_CHAT)
                    )
                    .messages(messageList)
                    .options(chatOptions)
                    .toolCallbacks(toolCallbackList)
                    .call()
                    .content();
            if (response == null || response.isEmpty()) {
                return CHAT_ERROR_RESPONSE;
            }
            return response;
        } catch (Exception e) {
            log.error("【AI 对话】完整对话失败：clientId={}", clientId, e);
            return CHAT_ERROR_RESPONSE;
        }
    }

    @PostMapping("/chat/stream")
    @Override
    public Flux<String> stream(@Valid @RequestBody AiChatRequest aiChatRequest) {

        String clientId = aiChatRequest.getClientId();
        if (isActiveChatClient(clientId)) {
            log.warn("【AI 对话】client 未启用或不存在：clientId={}", clientId);
            return Flux.just(CHAT_ERROR_RESPONSE);
        }

        String userMessage = aiChatRequest.getUserMessage();
        String ragTag = aiChatRequest.getRagTag();
        String sessionId = aiChatRequest.getSessionId();
        List<String> mcpIdList = aiChatRequest.getMcpIdList();
        Double temperature = aiChatRequest.getTemperature();
        Double presencePenalty = aiChatRequest.getPresencePenalty();
        Integer maxCompletionTokens = aiChatRequest.getMaxCompletionTokens();

        log.info("【AI 对话】流式对话开始：aiChatRequest={}", aiChatRequest);

        try {
            ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName(clientId), ChatClient.class);
            List<Message> messageList = augmentService.augmentRagMessage(userMessage, ragTag);
            SyncMcpToolCallbackProvider toolCallbackList = augmentService.augmentMcpTool(mcpIdList);
            ChatOptions chatOptions = OpenAiChatOptions.builder()
                    .temperature(temperature)
                    .presencePenalty(presencePenalty)
                    .maxCompletionTokens(maxCompletionTokens)
                    .build();
            return chatClient
                    .prompt()
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE_CHAT)
                    )
                    .messages(messageList)
                    .options(chatOptions)
                    .toolCallbacks(toolCallbackList)
                    .stream()
                    .content()
                    .doFinally(signalType -> log.info("【AI 对话】流式对话结束：clientId={}, signal={}", clientId, signalType))
                    .onErrorResume(e -> {
                        log.error("【AI 对话】流式对话失败：clientId={}", clientId, e);
                        return Flux.just(CHAT_ERROR_RESPONSE);
                    });
        } catch (Exception e) {
            log.error("【AI 对话】流式对话失败：clientId={}", clientId, e);
            return Flux.just(CHAT_ERROR_RESPONSE);
        }
    }


    @Override
    @PostMapping(value = "/armory")
    public Result<Void> armory(@Valid @RequestBody AiArmoryRequest aiArmoryRequest) {

        String armoryType = aiArmoryRequest.getArmoryType();
        String armoryId = aiArmoryRequest.getArmoryId();

        Set<String> armoryIdSet = new HashSet<>(Set.of(armoryId));
        dispatchService.dispatchArmoryStrategy(armoryType, armoryIdSet);

        return Result.success();
    }


    @PostMapping(value = "/rag/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Result<Void> uploadFile(@RequestPart("ragTag") String ragTag, @RequestPart("fileList") List<MultipartFile> fileList) {
        ragService.uploadTextFile(ragTag, fileList);
        return Result.success();
    }

    @PostMapping("/rag/git")
    @Override
    public Result<Void> uploadGitRepo(@RequestBody AiUploadRequest aiUploadRequest) {
        ragService.uploadGitRepo(aiUploadRequest);
        return Result.success();
    }

    private boolean isActiveChatClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return true;
        }
        List<QueryChatClientResponse> list = queryService.queryChatClientResponseList();
        if (list == null || list.isEmpty()) {
            return true;
        }
        return list.stream().noneMatch(item -> clientId.equals(item.getClientId()));
    }

    private boolean isActiveWorkAgent(String agentId) {
        if (agentId == null || agentId.isBlank()) {
            return false;
        }
        List<QueryWorkAgentResponse> list = queryService.queryWorkAgentResponseList();
        if (list == null || list.isEmpty()) {
            return false;
        }
        return list.stream().anyMatch(item -> agentId.equals(item.getAgentId()));
    }

}
