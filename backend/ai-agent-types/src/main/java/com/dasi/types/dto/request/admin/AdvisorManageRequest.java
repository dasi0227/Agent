package com.dasi.types.dto.request.admin;

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
public class AdvisorManageRequest {

    @NotNull
    private Long id;

    @NotBlank
    private String advisorId;

    @NotBlank
    private String advisorName;

    @NotBlank
    private String advisorType;

    private String advisorDesc;

    private Integer advisorOrder;

    private String advisorParam;

    @Builder.Default
    private Integer advisorStatus = 1;
}
