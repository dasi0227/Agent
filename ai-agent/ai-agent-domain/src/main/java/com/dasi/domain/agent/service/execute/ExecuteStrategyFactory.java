package com.dasi.domain.agent.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ExecuteStrategyFactory {

    private final Map<String, IExecuteStrategy> type2StrategyMap = new HashMap<>();

    public ExecuteStrategyFactory(Map<String, IExecuteStrategy> executeStrategyMap) {

        for (Map.Entry<String, IExecuteStrategy> entry : executeStrategyMap.entrySet()) {
            IExecuteStrategy executeStrategy = entry.getValue();
            type2StrategyMap.put(executeStrategy.getType(), executeStrategy);
        }

    }

    public IExecuteStrategy getStrategyByType(String type) {
        return type2StrategyMap.get(type);
    }

}
