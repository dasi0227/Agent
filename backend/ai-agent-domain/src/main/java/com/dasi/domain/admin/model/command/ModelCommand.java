package com.dasi.domain.admin.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCommand {

    private Long id;

    private String modelId;

    private String apiId;

    private String modelName;

    private String modelType;

    private Integer modelStatus;
}
