package com.dasi.trigger.http;

import com.dasi.api.IAiApi;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.service.augment.IAugmentService;
import com.dasi.domain.ai.service.dispatch.IDispatchService;
import com.dasi.domain.ai.service.rag.IRagService;
import com.dasi.types.dto.request.ArmoryRequest;
import com.dasi.types.dto.request.ChatRequest;
import com.dasi.types.dto.request.WorkRequest;
import com.dasi.types.dto.request.UploadGitRepoRequest;
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

    @Override
    @PostMapping(value = "/work/execute", produces = "text/event-stream")
    public SseEmitter execute(@Valid @RequestBody WorkRequest workRequest) {

        SseEmitter sseEmitter = new SseEmitter(0L);

        ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                .aiAgentId(workRequest.getAiAgentId())
                .userMessage(workRequest.getUserMessage())
                .sessionId(workRequest.getSessionId())
                .maxRound(workRequest.getMaxRound())
                .maxRetry(workRequest.getMaxRetry())
                .build();

        dispatchService.dispatchExecuteStrategy(executeRequestEntity, sseEmitter);

        return sseEmitter;
    }

    @PostMapping("/chat/complete")
    @Override
    public String complete(@Valid @RequestBody ChatRequest chatRequest) {

        String clientId = chatRequest.getClientId();
        String userMessage = chatRequest.getUserMessage();
        String ragTag = chatRequest.getRagTag();
        String sessionId = chatRequest.getSessionId();
        List<String> mcpIdList = chatRequest.getMcpIdList();
        Double temperature = chatRequest.getTemperature();
        Double presencePenalty = chatRequest.getPresencePenalty();
        Integer maxCompletionTokens = chatRequest.getMaxCompletionTokens();

        log.info("【模型对话】完整对话开始：chatRequest={}", chatRequest);

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
            log.error("【模型对话】完整对话失败：clientId={}", clientId, e);
            return CHAT_ERROR_RESPONSE;
        }
    }

    @PostMapping("/chat/stream")
    @Override
    public Flux<String> stream(@Valid @RequestBody ChatRequest chatRequest) {

        String clientId = chatRequest.getClientId();
        String userMessage = chatRequest.getUserMessage();
        String ragTag = chatRequest.getRagTag();
        String sessionId = chatRequest.getSessionId();
        List<String> mcpIdList = chatRequest.getMcpIdList();
        Double temperature = chatRequest.getTemperature();
        Double presencePenalty = chatRequest.getPresencePenalty();
        Integer maxCompletionTokens = chatRequest.getMaxCompletionTokens();

        log.info("【模型对话】流式对话开始：chatRequest={}", chatRequest);

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
                    .doFinally(signalType -> log.info("【模型对话】流式对话结束：clientId={}, signal={}", clientId, signalType))
                    .onErrorResume(e -> {
                        log.error("【模型对话】流式对话失败：clientId={}", clientId, e);
                        return Flux.just(CHAT_ERROR_RESPONSE);
                    });
        } catch (Exception e) {
            log.error("【模型对话】流式对话失败：clientId={}", clientId, e);
            return Flux.just(CHAT_ERROR_RESPONSE);
        }
    }


    @Override
    @PostMapping(value = "/armory")
    public Result<Void> armory(@Valid @RequestBody ArmoryRequest armoryRequest) {

        String armoryType = armoryRequest.getArmoryType();
        String armoryId = armoryRequest.getArmoryId();

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
    public Result<Void> uploadGitRepo(@RequestBody UploadGitRepoRequest uploadGitRepoRequest) {
        ragService.uploadGitRepo(uploadGitRepoRequest);
        return Result.success();
    }

}
