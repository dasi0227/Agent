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
public class FlowVO {
    private Long id;
    private String agentId;
    private String clientId;
    private String clientType;
    private String flowPrompt;
    private Integer flowSeq;
    private Integer flowStatus;
    private LocalDateTime updateTime;
}
