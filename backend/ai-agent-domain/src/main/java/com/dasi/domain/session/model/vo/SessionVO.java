package com.dasi.domain.session.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionVO {
    private Long id;
    private String sessionId;
    private String sessionUser;
    private String sessionTitle;
    private String sessionType;
    private LocalDateTime createTime;
}
