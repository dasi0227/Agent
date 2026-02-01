package com.dasi.types.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentManageRequest {

    private Long id;

    @NotBlank
    private String agentId;

    @NotBlank
    private String agentName;

    @NotBlank
    private String agentType;

    private String agentDesc;

    @Builder.Default
    private Integer agentStatus = 1;
}
