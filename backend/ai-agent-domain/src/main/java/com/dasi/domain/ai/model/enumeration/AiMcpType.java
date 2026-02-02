package com.dasi.domain.ai.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiMcpType {

    SSE("服务器发送事件", "sse"),
    STDIO("标准输入输出", "stdio")
    ;

    private String name;

    private String type;

    public static AiMcpType fromString(String str) {
        if (str == null) {
            throw new IllegalArgumentException("AiMcpType str is Null" );
        }

        for (AiMcpType mcpType : values()) {
            if (mcpType.type.equals(str)) {
                return mcpType;
            }
        }

        throw new IllegalArgumentException("Unknown AiMcpType str: " + str);
    }
}
