package com.dasi.domain.admin.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCommand {

    private Long id;

    private String agentId;

    private String agentName;

    private String agentType;

    private String agentDesc;

    private Integer agentStatus;
}
