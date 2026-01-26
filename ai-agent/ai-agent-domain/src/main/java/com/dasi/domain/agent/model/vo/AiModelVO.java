package com.dasi.domain.agent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiModelVO {

    /** 模型 id */
    private String modelId;

    /** 接口 id */
    private String apiId;

    /** 模型名称 */
    private String modelName;

    /** 模型类型 */
    private String modelType;

}
