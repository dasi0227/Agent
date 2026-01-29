package com.dasi.domain.ai.model.enumeration;

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
    REPLIER("任务回复专家", "replier"),
    ;

    private String name;

    private String type;

    public String getExceptionType() {
        return this.type + "_exception";
    }

    public String getContextKey() {
        return this.type + "_response";
    }

    public String getNodeName() {
        return this.type + "Node";
    }

}
