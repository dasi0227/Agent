package com.dasi.domain.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPrompt {

    private Long id;

    private String promptId;

    private String promptName;

    private String promptContent;

    private String promptDesc;

    private Integer promptStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
