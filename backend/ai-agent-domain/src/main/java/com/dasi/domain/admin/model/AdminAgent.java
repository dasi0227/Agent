package com.dasi.domain.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAgent {

    private Long id;

    private String agentId;

    private String agentName;

    private String agentType;

    private String agentDesc;

    private Integer agentStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
