package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiSectionType {

    ANALYZER_DEMAND("任务需求分析", "analyzer_demand"),
    ANALYZER_HISTORY("执行历史评估", "analyzer_history"),
    ANALYZER_STRATEGY("执行策略制定", "analyzer_strategy"),
    ANALYZER_PROGRESS("完成度评估", "analyzer_progress"),
    ANALYZER_STATUS("任务状态", "analyzer_status"),
    PERFORMER_TARGET("执行目标", "performer_target"),
    PERFORMER_PROCESS("执行过程", "performer_process"),
    PERFORMER_RESULT("执行结果", "performer_result"),
    SUPERVISOR_ISSUE("问题识别", "supervisor_issue"),
    SUPERVISOR_SUGGESTION("改进建议", "supervisor_suggestion"),
    SUPERVISOR_SCORE("质量评分", "supervisor_score"),
    SUPERVISOR_STATUS("监督状态", "supervisor_status"),
    SUMMARIZER_OVERVIEW("任务总结", "summarizer_overview"),
    INSPECTOR_MCP("MCP 工具", "inspector_mcp"),
    PLANNER_STEP("任务步骤", "planner_step"),
    ;

    private String name;

    private String type;

}
