package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 运行统计表（按日聚合） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStat {
    /** 自增 id */
    private Long id;

    /** 统计日期 */
    private LocalDate statDate;

    /** 统计类别 */
    private String statCategory;

    /** 统计键 */
    private String statKey;

    /** 统计值 */
    private String statValue;

    /** 统计计数 */
    private Integer statCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
