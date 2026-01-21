package com.dasi.domain.agent.service.execute.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.factory.ExecuteDynamicContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ExecuteRootNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {

        Map<String, AiFlowVO> aiFlowVOMap = agentRepository.queryAiFlowVOMapByAgentId(executeRequestEntity.getAiAgentId());

        // 客户端链
        executeDynamicContext.setAiFlowVOMap(aiFlowVOMap);
        // 当前步数
        executeDynamicContext.setStep(1);
        // 最大步数
        executeDynamicContext.setMaxStep(executeRequestEntity.getMaxStep());
        // 是否完成
        executeDynamicContext.setCompleted(false);
        // 执行历史
        executeDynamicContext.setExecutionHistory(new StringBuilder());
        // 用户原始需求
        executeDynamicContext.setOriginalTask(executeRequestEntity.getUserMessage());
        // 当前任务需求
        executeDynamicContext.setCurrentTask(executeRequestEntity.getUserMessage());

        log.info("【执行节点】ExecuteRootNode：userMessage={}", executeRequestEntity.getUserMessage());
        return router(executeRequestEntity, executeDynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {
        return getBean("executeAnalyzerNode");
    }

}
