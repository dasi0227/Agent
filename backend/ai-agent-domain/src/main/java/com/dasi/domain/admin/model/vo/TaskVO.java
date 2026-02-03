package com.dasi.domain.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskVO {

    private Long id;

    private String taskId;

    private String agentId;

    private String taskCron;

    private String taskDesc;

    private String taskParam;

    private Integer taskStatus;

    private LocalDateTime updateTime;
}
