package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiType {

    CLIENT("客户端", "client", "loadClientStrategy"),
    MODEL("对话模型", "model", "loadModelStrategy"),
    API("接口", "api", "loadApiStrategy"),
    MCP("工具", "mcp","loadMcpStrategy"),
    PROMPT("系统提示词", "prompt","loadPromptStrategy"),
    ADVISOR("顾问", "advisor","loadAdvisorStrategy"),
    ;

    private String name;

    private String type;

    private String loadStrategy;

    public static AiType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("AiType code is Null" );
        }

        for (AiType type : values()) {
            if (type.type.equals(code)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown AiType code: " + code);
    }

    public static String getLoadStrategyByCode(String code) {
        return fromCode(code).getLoadStrategy();
    }

    public String getBeanName(String id) {
        return "bean_" + id;
    }

}
