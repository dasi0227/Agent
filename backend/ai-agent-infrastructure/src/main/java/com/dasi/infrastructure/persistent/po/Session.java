package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 会话记录表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Session {
    /** 自增 id */
    private Long id;

    /** 会话 id */
    private String sessionId;

    /** 用户 id */
    private String sessionUser;

    /** 会话标题 */
    private String sessionTitle;

    /** 会话类型：chat/work */
    private String sessionType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
