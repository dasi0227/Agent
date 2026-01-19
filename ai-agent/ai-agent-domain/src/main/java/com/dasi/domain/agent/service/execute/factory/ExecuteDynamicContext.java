package com.dasi.domain.agent.service.execute.factory;

import com.dasi.domain.agent.model.vo.AiFlowVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteDynamicContext {

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