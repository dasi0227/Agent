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


    public static AiAdvisorType fromString(String str) {
        if (str == null) {
            throw new IllegalArgumentException("AiAdvisorType str is Null" );
        }

        for (AiAdvisorType advisorType : values()) {
            if (advisorType.type.equals(str)) {
                return advisorType;
            }
        }

        throw new IllegalArgumentException("Unknown AiAdvisorType str: " + str);
    }

}
