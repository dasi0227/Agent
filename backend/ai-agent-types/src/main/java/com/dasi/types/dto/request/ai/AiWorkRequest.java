package com.dasi.types.dto.request.ai;

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
public class AiWorkRequest {

    @NotBlank
    private String aiAgentId;

    @NotBlank
    private String userMessage;

    @NotBlank
    private String sessionId;

    @NotNull
    @Min(1)
    private Integer maxRound;

    @NotNull
    @Min(1)
    private Integer maxRetry;

}
