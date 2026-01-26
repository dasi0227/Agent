package com.dasi.infrastructure.persistent.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 客户端关联表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiConfig {
    /** 自增 id */
    private Long id;

    /** 客户端 id */
    private String clientId;

    /** 配置类型 */
    private String configType;

    /** 配置值 */
    private String configValue;

    /** 关联参数配置 */
    private String configParam;

    /** 状态：0-禁用，1-启用 */
    private Integer configStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
