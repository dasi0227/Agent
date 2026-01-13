package com.dasi.infrastructure.persistent.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 模型配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiModel {
    /** 自增 id */
    private Long id;

    /** 模型 id */
    private String modelId;

    /** 接口 id */
    private String apiId;

    /** 模型名称 */
    private String modelName;

    /** 模型类型 */
    private String modelType;

    /** 状态：0-禁用，1-启用 */
    private Integer modelStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
