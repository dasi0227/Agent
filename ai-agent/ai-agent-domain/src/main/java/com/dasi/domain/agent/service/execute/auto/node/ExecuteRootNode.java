package com.dasi.domain.agent.service.execute.auto.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteCommandEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.auto.factory.AutoExecuteStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ExecuteRootNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        Map<String, AiFlowVO> aiFlowVOMap = agentRepository.queryAiFlowVOMapByAgentId(executeCommandEntity.getAiAgentId());

        dynamicContext.setAiFlowVOMap(aiFlowVOMap);
        dynamicContext.setStep(0);
        dynamicContext.setCompleted(false);
        dynamicContext.setExecutionHistory(new StringBuilder());
        dynamicContext.setOriginalTask(executeCommandEntity.getUserMessage());
        dynamicContext.setCurrentTask(executeCommandEntity.getUserMessage());
        dynamicContext.setMaxStep(executeCommandEntity.getMaxStep());

        log.info("【执行节点】ExecuteRootNode：userMessage={}", executeCommandEntity.getUserMessage());
        return router(executeCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, AutoExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("executeAnalyzerNode");
    }

}
