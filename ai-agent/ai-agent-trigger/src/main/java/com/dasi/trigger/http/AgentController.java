package com.dasi.trigger.http;

import com.dasi.api.IAgentService;
import com.dasi.api.dto.AgentRequestDTO;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.execute.IExecuteStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController implements IAgentService {

    @Resource
    @Qualifier("executeLoopStrategy")
    private IExecuteStrategy executeLoopStrategy;

    @javax.annotation.Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    @PostMapping(value = "/agent", produces = "text/event-stream")
    public SseEmitter agent(@RequestBody AgentRequestDTO agentRequestDTO) {


        // 1. 创建流式输出对象
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 2. 构建执行命令实体
        ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                .aiAgentId(agentRequestDTO.getAiAgentId())
                .userMessage(agentRequestDTO.getUserMessage())
                .sessionId(agentRequestDTO.getSessionId())
                .maxStep(agentRequestDTO.getMaxStep())
                .build();

        threadPoolExecutor.execute(() -> {
            try {
                executeLoopStrategy.execute(executeRequestEntity, sseEmitter);
            } catch (Exception e) {
                try {
                    log.error("【Agent 执行】error={}", e.getMessage(), e);
                    sseEmitter.send(SseEmitter.event()
                            .name("error")
                            .data("执行异常：" + e.getMessage()));
                } catch (Exception ex) {
                    log.error("【Agent 执行】error={}", e.getMessage(), e);
                }
            }
        });

        return sseEmitter;
    }

}
