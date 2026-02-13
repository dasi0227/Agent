package com.dasi.trigger.http;

import com.dasi.api.IAiApi;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.service.augment.IAugmentService;
import com.dasi.domain.ai.service.dispatch.IDispatchService;
import com.dasi.domain.ai.service.rag.IRagService;
import com.dasi.domain.query.service.IQueryService;
import com.dasi.domain.session.model.enumeration.SessionType;
import com.dasi.domain.session.service.ISessionService;
import com.dasi.domain.util.message.IMessageService;
import com.dasi.domain.util.stat.IStatService;
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
import org.springframework.util.StringUtils;
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

    @Resource
    private ISessionService sessionService;

    @Resource
    private IMessageService messageService;

    @Resource
    private IStatService statService;

    @Override
    @PostMapping(value = "/work/execute", produces = "text/event-stream")
    public SseEmitter execute(@Valid @RequestBody AiWorkRequest aiWorkRequest) {

        String sessionId = aiWorkRequest.getSessionId();
        String userMessage = aiWorkRequest.getUserMessage();
        SseEmitter sseEmitter = new SseEmitter(0L);
        String agentId = aiWorkRequest.getAiAgentId();

        try {
            if (isInactiveWorkAgent(agentId)) {
                try {
                    sseEmitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI 未启用或不存在"));
                } catch (Exception e) {
                    log.error("【AI 执行】状态校验失败：agentId={}", agentId, e);
                } finally {
                    sseEmitter.complete();
                }
                return sseEmitter;
            }

            String invalidSessionReason = sessionService.validateSessionAccess(sessionId, SessionType.WORK.getType());
            if (StringUtils.hasText(invalidSessionReason)) {
                log.warn("【AI 执行】会话校验失败：sessionId={}, expectedType={}, error={}", sessionId, SessionType.WORK.getType(), invalidSessionReason);
                try {
                    sseEmitter.send(SseEmitter.event()
                            .name("error")
                            .data(invalidSessionReason));
                } catch (Exception e) {
                    log.error("【AI 执行】会话校验失败：sessionId={}", sessionId, e);
                } finally {
                    sseEmitter.complete();
                }
                return sseEmitter;
            }

            ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                    .agentId(agentId)
                    .userMessage(userMessage)
                    .sessionId(aiWorkRequest.getSessionId())
                    .maxRound(aiWorkRequest.getMaxRound())
                    .maxRetry(aiWorkRequest.getMaxRetry())
                    .build();

            dispatchService.dispatchExecuteStrategy(executeRequestEntity, sseEmitter);
            return sseEmitter;
        } finally {
            try {
                messageService.saveWorkUserMessage(sessionId, userMessage);
            } catch (Exception e) {
                log.warn("【AI 执行】持久化消息失败：sessionId={}, error={}", sessionId, e.getMessage());
            }
            try {
                statService.recordWorkUsage(agentId);
            } catch (Exception e) {
                log.warn("【AI 执行】记录统计失败：agentId={}, error={}", agentId, e.getMessage());
            }

        }
    }

    @PostMapping("/chat/complete")
    @Override
    public String complete(@Valid @RequestBody AiChatRequest aiChatRequest) {

        String clientId = aiChatRequest.getClientId();
        if (isInactiveChatClient(clientId)) {
            log.warn("【AI 对话】client 未启用或不存在：clientId={}", clientId);
            return CHAT_ERROR_RESPONSE;
        }

        String userMessage = aiChatRequest.getUserMessage();
        String ragTag = aiChatRequest.getRagTag();
        String sessionId = aiChatRequest.getSessionId();
        String invalidSessionReason = sessionService.validateSessionAccess(sessionId, SessionType.CHAT.getType());
        if (StringUtils.hasText(invalidSessionReason)) {
            log.warn("【AI 对话】会话校验失败：sessionId={}, expectedType={}, error={}", sessionId, SessionType.CHAT.getType(), invalidSessionReason);
            return invalidSessionReason;
        }
        List<String> mcpIdList = aiChatRequest.getMcpIdList();
        Double temperature = aiChatRequest.getTemperature();
        Double presencePenalty = aiChatRequest.getPresencePenalty();
        Integer maxCompletionTokens = aiChatRequest.getMaxCompletionTokens();

        log.info("【AI 对话】完整对话开始：aiChatRequest={}", aiChatRequest);
        String response = null;

        try {
            ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName(clientId), ChatClient.class);
            List<Message> messageList = augmentService.augmentRagMessage(userMessage, ragTag);
            SyncMcpToolCallbackProvider toolCallbackList = augmentService.augmentMcpTool(mcpIdList);
            ChatOptions chatOptions = OpenAiChatOptions.builder()
                    .temperature(temperature)
                    .presencePenalty(presencePenalty)
                    .maxCompletionTokens(maxCompletionTokens)
                    .build();

            response = chatClient.prompt()
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
        } finally {
            try {
                messageService.saveChatUserMessage(sessionId, userMessage);
            } catch (Exception e) {
                log.warn("【AI 对话】持久化用户消息失败：sessionId={}, error={}", sessionId, e.getMessage());
            }
            if (StringUtils.hasText(response) && !CHAT_ERROR_RESPONSE.equals(response)) {
                try {
                    messageService.saveChatAssistantMessage(sessionId, response);
                } catch (Exception e) {
                    log.warn("【AI 对话】持久化助手消息失败：sessionId={}, error={}", sessionId, e.getMessage());
                }
            }
            try {
                statService.recordChatUsage(clientId, mcpIdList);
            } catch (Exception e) {
                log.warn("【AI 对话】记录统计失败：clientId={}, error={}", clientId, e.getMessage());
            }
        }
    }

    @PostMapping("/chat/stream")
    @Override
    public Flux<String> stream(@Valid @RequestBody AiChatRequest aiChatRequest) {

        String clientId = aiChatRequest.getClientId();
        if (isInactiveChatClient(clientId)) {
            log.warn("【AI 对话】client 未启用或不存在：clientId={}", clientId);
            return Flux.just(CHAT_ERROR_RESPONSE);
        }

        String userMessage = aiChatRequest.getUserMessage();
        String ragTag = aiChatRequest.getRagTag();
        String sessionId = aiChatRequest.getSessionId();
        String invalidSessionReason = sessionService.validateSessionAccess(sessionId, SessionType.CHAT.getType());
        if (StringUtils.hasText(invalidSessionReason)) {
            log.warn("【AI 对话】会话校验失败：sessionId={}, expectedType={}, error={}", sessionId, SessionType.CHAT.getType(), invalidSessionReason);
            return Flux.just(invalidSessionReason);
        }
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
            StringBuilder answerBuffer = new StringBuilder();
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
                    .doOnNext(answerBuffer::append)
                    .doFinally(signalType -> {
                        if (answerBuffer.isEmpty()) {
                            return;
                        }
                        try {
                            messageService.saveChatAssistantMessage(sessionId, answerBuffer.toString());
                        } catch (Exception e) {
                            log.warn("【AI 对话】持久化消息失败：{}", e.getMessage());
                        }
                    })
                    .doFinally(signalType -> log.info("【AI 对话】流式对话结束：clientId={}, signal={}", clientId, signalType))
                    .onErrorResume(e -> Flux.just(CHAT_ERROR_RESPONSE));
        } catch (Exception e) {
            log.error("【AI 对话】流式对话失败：clientId={}", clientId, e);
            return Flux.just(CHAT_ERROR_RESPONSE);
        } finally {
            try {
                messageService.saveChatUserMessage(sessionId, userMessage);
            } catch (Exception e) {
                log.warn("【AI 对话】持久化用户消息失败：sessionId={}, error={}", sessionId, e.getMessage());
            }
            try {
                statService.recordChatUsage(clientId, mcpIdList);
            } catch (Exception e) {
                log.warn("【AI 对话】记录统计失败：clientId={}, error={}", clientId, e.getMessage());
            }
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
        try {
            ragService.uploadTextFile(ragTag, fileList);
            return Result.success();
        } catch (Exception e) {
            log.error("【上传知识库】文件上传失败：ragTag={}, error={}", ragTag, e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/rag/git")
    @Override
    public Result<Void> uploadGitRepo(@RequestBody AiUploadRequest aiUploadRequest) {
        try {
            ragService.uploadGitRepo(aiUploadRequest);
            return Result.success();
        } catch (Exception e) {
            log.error("【上传知识库】Git 上传失败：repoUrl={}, error={}", aiUploadRequest.getRepoUrl(), e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    private boolean isInactiveChatClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return true;
        }
        List<QueryChatClientResponse> list = queryService.queryChatClientResponseList();
        if (list == null || list.isEmpty()) {
            return true;
        }
        return list.stream().noneMatch(item -> clientId.equals(item.getClientId()));
    }

    private boolean isInactiveWorkAgent(String agentId) {
        if (agentId == null || agentId.isBlank()) {
            return true;
        }
        List<QueryWorkAgentResponse> list = queryService.queryWorkAgentResponseList();
        if (list == null || list.isEmpty()) {
            return true;
        }
        return list.stream().noneMatch(item -> agentId.equals(item.getAgentId()));
    }

}
