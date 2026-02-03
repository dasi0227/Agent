package com.dasi.types.dto.request.admin.manage;

import jakarta.validation.constraints.Min;
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
public class FlowManageRequest {

    private Long id;

    @NotBlank
    private String agentId;

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientRole;

    @NotBlank
    private String flowPrompt;

    @NotNull
    @Min(1)
    private Integer flowSeq;

}
