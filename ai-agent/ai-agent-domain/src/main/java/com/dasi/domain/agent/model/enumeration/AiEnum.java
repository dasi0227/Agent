package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiEnum {

    CLIENT("客户端", "client", "loadClientStrategy"),
    MODEL("对话模型", "model", "loadModelStrategy"),
    API("接口", "api", "loadApiStrategy"),
    MCP("工具", "mcp","loadMcpStrategy"),
    PROMPT("系统提示词", "prompt","loadPromptStrategy"),
    ADVISOR("顾问", "advisor","loadAdvisorStrategy"),
    ;

    private String name;

    private String code;

    private String loadStrategy;

    public static AiEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("AiEnum code is Null" );
        }

        for (AiEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown AiEnum code: " + code);
    }

    public static String getLoadStrategyByCode(String code) {
        return fromCode(code).getLoadStrategy();
    }

    public String getBeanName(String id) {
        return this.code + "_" + id;
    }

}
