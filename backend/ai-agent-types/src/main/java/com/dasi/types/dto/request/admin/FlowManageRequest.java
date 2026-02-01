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
public class FlowManageRequest {

    private Long id;

    @NotBlank
    private String agentId;

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientType;

    private String flowPrompt;

    private Integer flowSeq;

    @Builder.Default
    private Integer flowStatus = 1;
}
