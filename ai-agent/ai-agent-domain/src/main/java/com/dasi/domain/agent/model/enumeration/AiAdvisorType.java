package com.dasi.domain.agent.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiAdvisorType {

    CHAT_MEMORY("对话记忆", "ChatMemory"),
    RAG_ANSWER("知识库回答", "RagAnswer")
    ;

    private String name;

    private String code;

}
