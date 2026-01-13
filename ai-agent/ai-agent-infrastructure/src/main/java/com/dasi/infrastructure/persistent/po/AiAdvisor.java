package com.dasi.infrastructure.persistent.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 顾问配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAdvisor {
    /** 自增 id */
    private Long id;

    /** 顾问 id */
    private String advisorId;

    /** 顾问名称 */
    private String advisorName;

    /** 顾问类型 */
    private String advisorType;

    /** 顾问描述 */
    private String advisorDesc;

    /** 顾问顺序号 */
    private Integer advisorOrder;

    /** 顾问参数配置 */
    private String advisorParam;

    /** 状态：0-禁用，1-启用 */
    private Integer advisorStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
