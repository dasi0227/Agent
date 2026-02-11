package com.dasi.types.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiChatRequest {

    @NotBlank
    private String clientId;

    @NotBlank
    private String userMessage;

    @NotBlank
    private String sessionId;

    private List<String> mcpIdList;

    private String ragTag;

    @Builder.Default
    private Double temperature = 0.6;

    @Builder.Default
    private Double presencePenalty = 0.0;

    @Builder.Default
    private Integer maxCompletionTokens = 8192;

}
