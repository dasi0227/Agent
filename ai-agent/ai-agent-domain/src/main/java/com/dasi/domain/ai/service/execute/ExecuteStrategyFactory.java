package com.dasi.domain.ai.service.execute;

import com.dasi.domain.ai.repository.IAiRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ExecuteStrategyFactory {

    @Resource
    private IAiRepository aiRepository;

    private final Map<String, IExecuteStrategy> type2StrategyMap = new ConcurrentHashMap<>();

    public ExecuteStrategyFactory(Map<String, IExecuteStrategy> executeStrategyMap) {

        for (Map.Entry<String, IExecuteStrategy> entry : executeStrategyMap.entrySet()) {
            IExecuteStrategy executeStrategy = entry.getValue();
            type2StrategyMap.put(executeStrategy.getType(), executeStrategy);
        }

    }

    public IExecuteStrategy getStrategyByType(String type) {
        return type2StrategyMap.get(type);
    }

    public IExecuteStrategy getStrategyByAgentId(String aiAgentId) {
        String type = aiRepository.queryExecuteTypeByAgentId(aiAgentId);
        return getStrategyByType(type);
    }

}
