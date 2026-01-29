package com.dasi.domain.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiTaskVO {

    /** 任务 id */
    private String taskId;

    /** 智能体 id */
    private String agentId;

    /** 任务时间表达式 */
    private String taskCron;

    /** 任务描述 */
    private String taskDesc;

    /** 任务参数配置 */
    private TaskParam taskParam;

    /** 状态：0-禁用，1-启用 */
    private Integer taskStatus;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TaskParam {
        private Integer maxRetry;

        private Integer maxRound;

        private String userMessage;
    }

    public String buildSignature() {
        return String.join("|",
        this.agentId,
                this.taskCron,
                this.taskParam.maxRetry.toString(),
                this.taskParam.maxRound.toString(),
                this.taskParam.userMessage
        );
    }

}
