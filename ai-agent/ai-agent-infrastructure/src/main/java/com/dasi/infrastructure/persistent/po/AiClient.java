package com.dasi.infrastructure.persistent.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 客户端配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClient {
    /** 自增 ID */
    private Long id;

    /** 客户端 id */
    private String clientId;

    /** 客户端名称 */
    private String clientName;

    /** 客户端描述 */
    private String clientDesc;

    /** 状态：0-禁用，1-启用 */
    private Integer clientStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
