package com.dasi.domain.agent.service.execute.step;

import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.entity.ExecuteResponseEntity;
import com.dasi.domain.agent.service.execute.ExecuteContext;
import com.dasi.domain.agent.service.execute.IExecuteStrategy;
import com.dasi.domain.agent.service.execute.step.node.ExecuteStepRootNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class ExecuteStepStrategy implements IExecuteStrategy {

    @Resource
    private ExecuteStepRootNode executeStepRootNode;

    @Override
    public void execute(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) throws Exception {

        ExecuteContext executeContext = new ExecuteContext();
        executeContext.setValue("sseEmitter", sseEmitter);

        log.info("【Agent 执行】执行 StepStrategy");
        executeStepRootNode.apply(executeRequestEntity, executeContext);

        try {
            ExecuteResponseEntity completeResult = ExecuteResponseEntity.createCompleteResponse("执行完成", executeRequestEntity.getSessionId());
            sseEmitter.send(SseEmitter.event()
                    .name("complete")
                    .data(completeResult));
        } catch (Exception e) {
            log.error("【Agent 执行】error={}", e.getMessage(), e);
        }
    }


}
