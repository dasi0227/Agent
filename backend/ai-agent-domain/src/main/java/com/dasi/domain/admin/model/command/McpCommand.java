package com.dasi.domain.admin.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpCommand {

    private Long id;

    private String mcpId;

    private String mcpName;

    private String mcpType;

    private String mcpConfig;

    private String mcpDesc;

    private Integer mcpTimeout;

    private Integer mcpChat;

    private Integer mcpStatus;
}
