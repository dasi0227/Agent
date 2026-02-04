package com.dasi.domain.session.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO {
    private Long id;
    private String messageContent;
    private String messageRole;
    private String messageType;
    private Integer messageSeq;
    private LocalDateTime createTime;
}
