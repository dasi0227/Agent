package com.dasi.domain.admin.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowCommand {

    private Long id;

    private String agentId;

    private String clientId;

    private String clientType;

    private String flowPrompt;

    private Integer flowSeq;

    private Integer flowStatus;
}
