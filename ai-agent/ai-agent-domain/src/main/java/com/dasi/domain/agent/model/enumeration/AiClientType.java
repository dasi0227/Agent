package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiClientType {

    ANALYZER("任务分析和状态判断", "analyzer"),
    PERFORMER("具体任务执行", "performer"),
    SUPERVISOR("质量检查和优化", "supervisor"),
    SUMMARIZER("任务总结记录", "summarizer"),
    ;

    private String name;

    private String type;

    public String getExceptionType() {
        return this.type + "_exception";
    }

}
