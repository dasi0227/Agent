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
public class ApiManageRequest {

    @NotBlank
    private String apiId;

    @NotBlank
    private String apiBaseUrl;

    @NotBlank
    private String apiKey;

    private String apiCompletionsPath;

    private String apiEmbeddingsPath;

    @Builder.Default
    private Integer apiStatus = 1;

}
