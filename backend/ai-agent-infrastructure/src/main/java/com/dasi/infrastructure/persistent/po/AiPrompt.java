package com.dasi.infrastructure.persistent.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 系统提示词配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiPrompt {
    /** 自增 id */
    private Long id;

    /** 提示词 id */
    private String promptId;

    /** 提示词名称 */
    private String promptName;

    /** 提示词内容 */
    private String promptContent;

    /** 提示词描述 */
    private String promptDesc;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
