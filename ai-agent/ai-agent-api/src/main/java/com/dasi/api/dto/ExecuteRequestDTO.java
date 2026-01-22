package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteRequestDTO {

    @NotBlank
    private String executeType;

    @NotBlank
    private String aiAgentId;

    @NotBlank
    private String userMessage;

    @NotBlank
    private String sessionId;

    @NotNull
    @Min(1)
    private Integer maxRound;

}
