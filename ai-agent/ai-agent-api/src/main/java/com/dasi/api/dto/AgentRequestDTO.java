package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentRequestDTO {

    private String aiAgentId;

    private String userMessage;

    private String sessionId;

    private Integer maxStep;

}
