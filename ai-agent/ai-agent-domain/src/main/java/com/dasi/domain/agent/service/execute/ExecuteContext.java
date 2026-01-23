package com.dasi.domain.agent.service.execute;

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
public class ExecuteContext {

    // ================== 适用于 Loop ==================
    /** 轮次 */
    private Integer round;

    /** 最大轮次 */
    private Integer maxRound;

    /** 是否完成 */
    private Boolean completed;

    /** 当前任务 */
    private String currentTask;

    // ================== 适用于 Step ==================

    private Integer step;

    private Integer maxRetry;

    // ================== 通用 ==================
    /** 执行历史 */
    private StringBuilder executionHistory;

    /** 用户需求 */
    private String userMessage;

    /** 执行工作流 */
    private Map<String, AiFlowVO> aiFlowVOMap;

    // ================== 上下文存储 ==================
    private Map<String, Object> dataObjects = new HashMap<>();

    public <T> void setValue(String key, T value) {
        dataObjects.put(key, value);
    }

    public <T> T getValue(String key) {
        return (T) dataObjects.get(key);
    }

}