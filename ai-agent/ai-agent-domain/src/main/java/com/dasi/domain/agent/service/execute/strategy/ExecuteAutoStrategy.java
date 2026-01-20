package com.dasi.domain.agent.service.execute.strategy;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteAutoResultEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.execute.factory.ExecuteAutoStrategyFactory;
import com.dasi.domain.agent.service.execute.factory.ExecuteDynamicContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class ExecuteAutoStrategy implements IExecuteStrategy {

    @Resource
    private ExecuteAutoStrategyFactory executeAutoStrategyFactory;

    @Override
    public void execute(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) throws Exception {

        StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> executeStrategyHandler = executeAutoStrategyFactory.getExecuteRootNode();

        ExecuteDynamicContext executeDynamicContext = new ExecuteDynamicContext();
        executeDynamicContext.setValue("sseEmitter", sseEmitter);

        executeStrategyHandler.apply(executeRequestEntity, executeDynamicContext);

        try {
            ExecuteAutoResultEntity completeResult = ExecuteAutoResultEntity.createCompleteResult("执行完成", executeRequestEntity.getSessionId());
            sseEmitter.send(SseEmitter.event()
                    .name("complete")
                    .data(completeResult));
        } catch (Exception e) {
            log.error("【Agent 执行】error={}", e.getMessage(), e);
        }
    }

}
