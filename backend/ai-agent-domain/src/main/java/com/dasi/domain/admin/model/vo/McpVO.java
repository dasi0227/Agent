package com.dasi.domain.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpVO {
    private Long id;
    private String mcpId;
    private String mcpName;
    private String mcpType;
    private String mcpConfig;
    private String mcpDesc;
    private Integer mcpTimeout;
    private Integer mcpChat;
    private Integer mcpStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
