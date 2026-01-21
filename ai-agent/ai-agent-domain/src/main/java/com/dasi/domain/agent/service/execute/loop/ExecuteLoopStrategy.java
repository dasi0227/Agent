package com.dasi.domain.agent.service.execute.loop;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.execute.IExecuteStrategy;
import com.dasi.domain.agent.service.execute.loop.model.ExecuteLoopContext;
import com.dasi.domain.agent.service.execute.loop.model.ExecuteLoopResult;
import com.dasi.domain.agent.service.execute.loop.node.ExecuteRootNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class ExecuteLoopStrategy implements IExecuteStrategy {

    @Resource
    private ExecuteRootNode executeRootNode;

    public StrategyHandler<ExecuteRequestEntity, ExecuteLoopContext, String> getExecuteRootNode() {
        return executeRootNode;
    }

    @Override
    public void execute(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) throws Exception {

        ExecuteLoopContext executeLoopContext = new ExecuteLoopContext();
        executeLoopContext.setValue("sseEmitter", sseEmitter);

        executeRootNode.apply(executeRequestEntity, executeLoopContext);

        try {
            ExecuteLoopResult completeResult = ExecuteLoopResult.createCompleteResult("执行完成", executeRequestEntity.getSessionId());
            sseEmitter.send(SseEmitter.event()
                    .name("complete")
                    .data(completeResult));
        } catch (Exception e) {
            log.error("【Agent 执行】error={}", e.getMessage(), e);
        }
    }

}
