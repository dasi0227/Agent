package com.dasi.domain.ai.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiAdvisorType {

    MEMORY("对话记忆", "Memory"),
    RAG("知识库回答", "Rag")
    ;

    private String name;

    private String type;


    public static AiAdvisorType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("AiAdvisorType code is Null" );
        }

        for (AiAdvisorType type : values()) {
            if (type.type.equals(code)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown AiAdvisorType code: " + code);
    }

}
