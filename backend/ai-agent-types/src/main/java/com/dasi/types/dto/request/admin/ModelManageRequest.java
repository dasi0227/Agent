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
public class ModelManageRequest {

    @NotNull
    private Long id;

    @NotBlank
    private String modelId;

    @NotBlank
    private String apiId;

    @NotBlank
    private String modelName;

    @NotBlank
    private String modelType;

    @Builder.Default
    private Integer modelStatus = 1;
}
