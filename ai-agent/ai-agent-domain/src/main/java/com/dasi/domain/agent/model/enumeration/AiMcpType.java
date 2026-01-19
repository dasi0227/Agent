package com.dasi.domain.agent.model.enumeration;

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

    public static AiMcpType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("AiMcpType code is Null" );
        }

        for (AiMcpType type : values()) {
            if (type.type.equals(code)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown AiMcpType code: " + code);
    }
}
