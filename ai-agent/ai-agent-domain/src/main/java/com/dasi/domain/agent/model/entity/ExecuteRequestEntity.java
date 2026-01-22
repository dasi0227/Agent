package com.dasi.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteRequestEntity {

    private String aiAgentId;

    private String userMessage;

    private String sessionId;

    private Integer maxRound;

}
