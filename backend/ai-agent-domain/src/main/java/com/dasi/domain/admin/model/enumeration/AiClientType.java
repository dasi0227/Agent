package com.dasi.domain.admin.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiClientType {

    WORK("智能体", "work"),
    CHAT("对话", "chat")
    ;

    private String name;

    private String type;

}
