package com.dasi.domain.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelVO {
    private Long id;
    private String modelId;
    private String apiId;
    private String modelName;
    private String modelType;
    private Integer modelStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
