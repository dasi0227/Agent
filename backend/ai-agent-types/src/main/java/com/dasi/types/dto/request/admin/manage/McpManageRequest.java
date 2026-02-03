package com.dasi.types.dto.request.admin.manage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpManageRequest {

    private Long id;

    @NotBlank
    private String mcpId;

    @NotBlank
    private String mcpName;

    @NotBlank
    private String mcpType;

    @NotNull
    private String mcpConfig;

    private String mcpDesc;

    private Integer mcpTimeout;

    private Integer mcpChat;

}
