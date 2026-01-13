package com.dasi.infrastructure.persistent.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 智能体-客户端关联表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiFlow {
    /** 主键ID */
    private Long id;

    /** 智能体ID */
    private String agentId;

    /** 客户端ID */
    private String clientId;

    /** 序列号(执行顺序) */
    private Integer flowSeq;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
