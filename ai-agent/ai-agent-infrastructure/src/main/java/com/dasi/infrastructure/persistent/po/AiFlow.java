package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    /** 客户端ID */
    private String clientName;

    /** 客户端ID */
    private String clientType;

    /** 工作流提示词 */
    private String flowPrompt;

    /** 工作流顺序 */
    private Integer flowSeq;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
