package com.dasi.domain.admin.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCommand {

    private Long id;

    private String apiId;

    private String apiBaseUrl;

    private String apiKey;

    private String apiCompletionsPath;

    private String apiEmbeddingsPath;

    private Integer apiStatus;
}
