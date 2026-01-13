package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiEnum {

    CLIENT("客户端", "client", "ai_client_", "loadClientStrategy"),
    MODEL("对话模型", "model", "ai_model_", "loadModelStrategy"),
    API("接口", "api", "ai_api_", "loadApiStrategy"),
    MCP("MCP 工具", "mcp", "ai_mcp_", "loadMcpStrategy"),
    PROMPT("系统提示词", "prompt", "ai_prompt_", "loadPromptStrategy"),
    ADVISOR("洗头膏顾问", "advisor", "ai_advisor_", "loadAdvisorStrategy"),
    ;

    private String name;

    private String code;

    private String beanNamePrefix;

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

    public String getBeanName(String apiId) {
        return this.beanNamePrefix + apiId;
    }
}
