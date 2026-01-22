package com.dasi.domain.agent.service.execute.step.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.execute.AbstractExecuteNode;
import com.dasi.domain.agent.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service(value = "runnerNode")
public class ExecuteRunnerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return "";
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return null;
    }

}
