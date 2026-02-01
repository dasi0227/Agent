package com.dasi.types.dto.request.admin.manage;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientManageRequest {

    private Long id;

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientType;

    @NotBlank
    private String modelId;

    private String modelName;

    @NotBlank
    private String clientName;

    private String clientDesc;

    @Builder.Default
    private Integer clientStatus = 1;
}
