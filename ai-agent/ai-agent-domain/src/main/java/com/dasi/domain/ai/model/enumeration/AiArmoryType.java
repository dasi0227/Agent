package com.dasi.domain.ai.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiArmoryType {

    ARMORY_AGENT("AI 智能体", "agent"),
    ARMORY_CHAT("AI 对话", "chat")
    ;

    private String name;

    private String type;

}
