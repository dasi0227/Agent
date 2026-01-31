package com.dasi.domain.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApi {

    private Long id;

    private String apiId;

    private String apiBaseUrl;

    private String apiKey;

    private String apiCompletionsPath;

    private String apiEmbeddingsPath;

    private Integer apiStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
