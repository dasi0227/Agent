package com.dasi.domain.session.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum MessageRole {

    USER("用户", "user"),
    ASSISTANT("助手", "assistant")
    ;

    private String name;

    private String role;

}
