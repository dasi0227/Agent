package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 工具配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiMcp {

    /** 自增 id */
    private Long id;

    /** 工具 id */
    private String mcpId;

    /** 工具名称 */
    private String mcpName;

    /** 工具类型 */
    private String mcpType;

    /** 工具路径 */
    private String mcpConfig;

    /** 请求超时时间 */
    private Integer mcpTimeout;

    /** 工具描述 */
    private String mcpDesc;

    /** 状态：0-禁用，1-启用 */
    private Integer mcpStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
