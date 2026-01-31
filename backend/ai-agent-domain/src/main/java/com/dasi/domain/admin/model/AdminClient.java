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
public class AdminClient {

    private Long id;

    private String clientId;

    private String clientType;

    private String modelId;

    private String modelName;

    private String clientName;

    private String clientDesc;

    private Integer clientStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
