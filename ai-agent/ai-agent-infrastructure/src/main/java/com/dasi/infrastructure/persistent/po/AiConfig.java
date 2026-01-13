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

    /** 源类型 */
    private String sourceType;

    /** 源 id */
    private String sourceId;

    /** 目标类型 */
    private String targetType;

    /** 目标 id */
    private String targetId;

    /** 关联参数配置 */
    private String configParam;

    /** 状态：0-禁用，1-启用 */
    private Integer configStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
