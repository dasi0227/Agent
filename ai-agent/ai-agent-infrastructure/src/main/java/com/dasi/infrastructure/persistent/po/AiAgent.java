package com.dasi.infrastructure.persistent.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 智能体配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgent {
    /** 自增 id */
    private Long id;

    /** 全局 id */
    private String agentId;

    /** 智能体名称 */
    private String agentName;

    /** 智能体渠道 */
    private String agentChannel;

    /** 智能体描述 */
    private String agentDesc;

    /** 状态：0-禁用，1-启用 */
    private Integer agentStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
