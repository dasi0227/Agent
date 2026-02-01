package com.dasi.domain.ai.service.execute.step.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.vo.AiFlowVO;
import com.dasi.domain.ai.service.execute.AbstractExecuteNode;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.dasi.domain.ai.model.enumeration.AiClientType.INSPECTOR;

@Slf4j
@Service
public class ExecuteStepRootNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        Map<String, AiFlowVO> aiFlowVOMap = aiRepository.queryAiFlowVOMapByAgentId(executeRequestEntity.getAgentId());

        // 客户端链
        executeContext.setAiFlowVOMap(aiFlowVOMap);
        // 执行历史
        executeContext.setExecutionHistory(new StringBuilder());
        // 用户原始需求
        executeContext.setUserMessage(executeRequestEntity.getUserMessage());
        // 最大重试次数
        executeContext.setMaxRetry(executeRequestEntity.getMaxRetry());

        log.info("【执行节点】ExecuteStepRootNode：userMessage={}", executeRequestEntity.getUserMessage());
        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return getBean(INSPECTOR.getNodeName());
    }

}
