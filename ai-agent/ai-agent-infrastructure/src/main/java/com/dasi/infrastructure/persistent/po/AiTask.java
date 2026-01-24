package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 智能体任务调度配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiTask {
    /** 自增 id */
    private Long id;

    /** 任务 id */
    private String taskId;

    /** 智能体 id */
    private String agentId;

    /** 任务时间表达式 */
    private String taskCron;

    /** 任务描述 */
    private String taskDesc;

    /** 任务参数配置 */
    private String taskParam;

    /** 状态：0-禁用，1-启用 */
    private Integer taskStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
