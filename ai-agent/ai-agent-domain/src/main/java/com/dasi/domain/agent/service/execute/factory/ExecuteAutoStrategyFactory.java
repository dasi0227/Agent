package com.dasi.domain.agent.service.execute.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.execute.node.ExecuteRootNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExecuteAutoStrategyFactory {

    @Resource
    private ExecuteRootNode executeRootNode;

    public StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> getExecuteRootNode() {
        return executeRootNode;
    }
}
