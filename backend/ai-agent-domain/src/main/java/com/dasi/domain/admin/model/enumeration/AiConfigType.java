package com.dasi.domain.admin.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiConfigType {

    PROMPT("提示词", "prompt"),
    MCP("工具", "mcp"),
    ADVISOR("顾问", "advisor")
    ;

    private String name;

    private String type;

}
