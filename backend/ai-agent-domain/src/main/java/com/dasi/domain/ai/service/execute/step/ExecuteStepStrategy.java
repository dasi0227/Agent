package com.dasi.domain.ai.service.execute.step;

import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.entity.ExecuteResponseEntity;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import com.dasi.domain.ai.service.execute.IExecuteStrategy;
import com.dasi.domain.ai.service.execute.step.node.ExecuteStepRootNode;
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

        log.info("【Agent 执行】策略=StepStrategy");
        executeStepRootNode.apply(executeRequestEntity, executeContext);

        ExecuteResponseEntity completeResult = ExecuteResponseEntity.createCompleteResponse("执行完成", executeRequestEntity.getSessionId());
        sseEmitter.send(SseEmitter.event()
                .name("complete")
                .data(completeResult));
    }

    @Override
    public String getType() {
        return "step";
    }

}
