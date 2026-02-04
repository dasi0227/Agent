package com.dasi.domain.session.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SessionType {

    CHAT("chat", "Chat"),
    WORK("work", "Work");

    private String type;

    private String name;

    @JsonValue
    public String value() {
        return type;
    }

    @JsonCreator
    public static SessionType fromType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        for (SessionType item : values()) {
            if (item.type.equalsIgnoreCase(type)) {
                return item;
            }
        }
        throw new IllegalStateException("找不到类型：" + type);
    }
}
