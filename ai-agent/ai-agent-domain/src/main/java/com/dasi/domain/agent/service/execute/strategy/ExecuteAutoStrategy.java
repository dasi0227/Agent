package com.dasi.domain.agent.service.execute.strategy;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSON;
import com.dasi.domain.agent.model.entity.ExecuteAutoResultEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.execute.factory.ExecuteAutoStrategyFactory;
import com.dasi.domain.agent.service.execute.factory.ExecuteDynamicContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Slf4j
@Service
public class ExecuteAutoStrategy implements IExecuteStrategy {

    @Resource
    private ExecuteAutoStrategyFactory executeAutoStrategyFactory;

    @Override
    public void execute(ExecuteRequestEntity executeRequestEntity, ResponseBodyEmitter responseBodyEmitter) throws Exception {

        StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> executeStrategyHandler = executeAutoStrategyFactory.getExecuteRootNode();

        ExecuteDynamicContext executeDynamicContext = new ExecuteDynamicContext();
        executeDynamicContext.setValue("responseBodyEmitter", responseBodyEmitter);

        executeStrategyHandler.apply(executeRequestEntity, executeDynamicContext);

        try {
            ExecuteAutoResultEntity completeResult = ExecuteAutoResultEntity.createCompleteResult("执行完成", executeRequestEntity.getSessionId());
            String sseData = "data: " + JSON.toJSONString(completeResult) + "\n\n";
            responseBodyEmitter.send(sseData);
        } catch (Exception e) {
            log.error("【Agent 执行】error={}", e.getMessage(), e);
        }
    }

}
