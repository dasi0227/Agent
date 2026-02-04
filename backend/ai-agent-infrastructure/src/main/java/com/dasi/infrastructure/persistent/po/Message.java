package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 会话消息记录表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    /** 自增 id */
    private Long id;

    /** 会话 id */
    private String sessionId;

    /** 消息内容 */
    private String messageContent;

    /** 消息角色：user/assistant */
    private String messageRole;

    /** 消息类型：chat/work-sse/work-answer */
    private String messageType;

    /** 消息编号 */
    private Integer messageSeq;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
