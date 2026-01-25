package com.dasi.trigger.http;

import com.dasi.api.IChatService;
import com.dasi.domain.chat.service.query.IQueryService;
import com.dasi.types.model.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.MODEL;
import static com.dasi.types.common.MessageConstant.CHAT_ERROR_RESPONSE;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController implements IChatService {

    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private IQueryService queryService;

    @GetMapping("/complete")
    @Override
    public String complete(String modelId, String message) {
        String beanName = MODEL.getBeanName(modelId);

        try {
            ChatModel chatModel = applicationContext.getBean(beanName, ChatModel.class);
            String response = chatModel.call(message).trim();
            if (response.isEmpty()) {
                return CHAT_ERROR_RESPONSE;
            }
            return response;
        } catch (Exception e) {
            log.error("【模型对话】模型 complete 对话失败：modelId={}", modelId, e);
            return CHAT_ERROR_RESPONSE;
        }
    }

    @GetMapping("/stream")
    @Override
    public Flux<String> stream(String modelId, String message) {
        String beanName = MODEL.getBeanName(modelId);

        try {
            ChatModel chatModel = applicationContext.getBean(beanName, ChatModel.class);
            return chatModel.stream(message)
                    .onErrorResume(e -> {
                        log.error("【模型对话】模型 stream 对话失败：modelId={}", modelId, e);
                        return Flux.just(CHAT_ERROR_RESPONSE);
                    });
        } catch (Exception e) {
            log.error("【模型对话】模型 stream 对话失败：modelId={}", modelId, e);
            return Flux.just(CHAT_ERROR_RESPONSE);
        }
    }

    @Override
    public Result<List<String>> queryModelIdList() {
        List<String> modelIdList = queryService.queryModelIdList();
        return Result.success(modelIdList);
    }


}
