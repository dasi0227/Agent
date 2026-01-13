package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiConfigType {

    CLIENT("客户端", "client", "ai_client_", "loadClientStrategy"),
    MODEL("对话模型", "model", "ai_model_", "loadModelStrategy"),
    MCP("MCP 工具", "mcp", "ai_mcp_", "loadMcpStrategy"),
    PROMPT("系统提示词", "prompt", "ai_prompt_", "loadPromptStrategy"),
    ADVISOR("洗头膏顾问", "advisor", "ai_advisor_", "loadAdvisorStrategy"),
    ;

    private String name;

    private String code;

    private String beanNamePrefix;

    private String loadStrategy;

    public static AiConfigType fromCode(String code) {
        for (AiConfigType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AiType code: " + code);
    }
}
