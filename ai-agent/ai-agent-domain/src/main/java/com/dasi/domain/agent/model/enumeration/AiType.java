package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiType {

    CLIENT("客户端", "client"),
    MODEL("对话模型", "model"),
    API("接口", "api"),
    MCP("工具", "mcp"),
    PROMPT("系统提示词", "prompt"),
    ADVISOR("顾问", "advisor"),
    ;

    private String name;

    private String type;

    public static AiType fromType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("AiType type is Null" );
        }

        for (AiType value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown AiType type: " + type);
    }

    public String getBeanName(String id) {
        return "bean_" + id;
    }

}
