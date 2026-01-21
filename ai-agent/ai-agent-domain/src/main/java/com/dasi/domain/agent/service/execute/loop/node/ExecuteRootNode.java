package com.dasi.domain.agent.service.execute.loop.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.loop.model.ExecuteLoopContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ExecuteRootNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteLoopContext executeLoopContext) throws Exception {

        Map<String, AiFlowVO> aiFlowVOMap = agentRepository.queryAiFlowVOMapByAgentId(executeRequestEntity.getAiAgentId());

        // 客户端链
        executeLoopContext.setAiFlowVOMap(aiFlowVOMap);
        // 当前步数
        executeLoopContext.setStep(1);
        // 最大步数
        executeLoopContext.setMaxStep(executeRequestEntity.getMaxStep());
        // 是否完成
        executeLoopContext.setCompleted(false);
        // 执行历史
        executeLoopContext.setExecutionHistory(new StringBuilder());
        // 用户原始需求
        executeLoopContext.setOriginalTask(executeRequestEntity.getUserMessage());
        // 当前任务需求
        executeLoopContext.setCurrentTask(executeRequestEntity.getUserMessage());

        log.info("【执行节点】ExecuteRootNode：userMessage={}", executeRequestEntity.getUserMessage());
        return router(executeRequestEntity, executeLoopContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteLoopContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteLoopContext executeLoopContext) throws Exception {
        return getBean("executeAnalyzerNode");
    }

}
