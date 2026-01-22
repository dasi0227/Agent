package com.dasi.domain.agent.service.execute.step.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.AbstractExecuteNode;
import com.dasi.domain.agent.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.dasi.domain.agent.model.enumeration.AiClientType.INSPECTOR;

@Slf4j
@Service
public class ExecuteStepRootNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        Map<String, AiFlowVO> aiFlowVOMap = agentRepository.queryAiFlowVOMapByAgentId(executeRequestEntity.getAiAgentId());

        // 客户端链
        executeContext.setAiFlowVOMap(aiFlowVOMap);
        // 执行历史
        executeContext.setExecutionHistory(new StringBuilder());
        // 用户原始需求
        executeContext.setUserMessage(executeRequestEntity.getUserMessage());

        log.info("【执行节点】ExecutePlanRootNode：userMessage={}", executeRequestEntity.getUserMessage());
        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return getBean(INSPECTOR.getNodeName());
    }

}
