package com.dasi.domain.agent.service.execute.auto.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteCommandEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.auto.node.ExecuteRootNode;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AutoExecuteStrategyFactory {

    @Resource
    private ExecuteRootNode executeRootNode;

    public StrategyHandler<ExecuteCommandEntity, DynamicContext, String> getExecuteNode() {
        return executeRootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private int step;

        private Boolean completed;

        private int maxStep;

        private String originalTask;

        private String currentTask;

        private StringBuilder executionHistory;

        private Map<String, AiFlowVO> aiFlowVOMap;

        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }

    }

}
