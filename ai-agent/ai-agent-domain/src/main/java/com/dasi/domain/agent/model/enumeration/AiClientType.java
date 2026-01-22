package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiClientType {

    ANALYZER("任务分析专家", "analyzer"),
    PERFORMER("任务执行专家", "performer"),
    SUPERVISOR("任务监督专家", "supervisor"),
    SUMMARIZER("任务总结专家", "summarizer"),
    INSPECTOR("任务审查专家", "inspector"),
    PLANNER("任务规划专家", "planner"),
    RUNNER("任务运行专家", "runner"),
    ;

    private String name;

    private String type;

    public String getExceptionType() {
        return this.type + "_exception";
    }

}
