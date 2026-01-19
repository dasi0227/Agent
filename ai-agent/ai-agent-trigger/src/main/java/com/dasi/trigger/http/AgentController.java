package com.dasi.trigger.http;

import com.dasi.api.IAgentService;
import com.dasi.api.dto.AgentAutoRequestDTO;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.execute.strategy.IExecuteStrategy;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController implements IAgentService {

    @Resource
    @Qualifier("executeAutoStrategy")
    private IExecuteStrategy executeAutoStrategy;

    @javax.annotation.Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    @PostMapping("/agent/auto")
    public ResponseBodyEmitter agentAuto(@RequestBody AgentAutoRequestDTO agentAutoRequestDTO, HttpServletResponse response) {

        try {

            response.setContentType("text/event-stream;charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");

            // 1. 创建流式输出对象
            ResponseBodyEmitter responseBodyEmitter = new ResponseBodyEmitter(Long.MAX_VALUE);

            // 2. 构建执行命令实体
            ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                    .aiAgentId(agentAutoRequestDTO.getAiAgentId())
                    .userMessage(agentAutoRequestDTO.getUserMessage())
                    .sessionId(agentAutoRequestDTO.getSessionId())
                    .maxStep(agentAutoRequestDTO.getMaxStep())
                    .build();

            // 3. 异步执行
            threadPoolExecutor.execute(() -> {
                try {
                    log.info("【Agent 执行】executeRequestEntity={}", executeRequestEntity);
                    executeAutoStrategy.execute(executeRequestEntity, responseBodyEmitter);
                } catch (Exception e) {
                    try {
                        log.error("【Agent 执行】error={}", e.getMessage(), e);
                        responseBodyEmitter.send("data: 执行异常" + e.getMessage());
                    } catch (Exception ex) {
                        log.error("【Agent 执行】error={}", e.getMessage(), e);
                    }
                } finally {
                    responseBodyEmitter.complete();
                }
            });

            return responseBodyEmitter;

        } catch (Exception e) {

            ResponseBodyEmitter responseBodyEmitter = new ResponseBodyEmitter(Long.MAX_VALUE);

            try {
                log.error("【Agent 执行】error={}", e.getMessage(), e);
                responseBodyEmitter.send("data: 执行异常" + e.getMessage());
            } catch (Exception ex) {
                log.error("【Agent 执行】error={}", ex.getMessage(), ex);
            }

            return responseBodyEmitter;
        }

    }

}
