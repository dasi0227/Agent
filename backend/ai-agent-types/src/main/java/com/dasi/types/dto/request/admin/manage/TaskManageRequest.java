package com.dasi.types.dto.request.admin.manage;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskManageRequest {

    private Long id;

    @NotBlank
    private String taskId;

    @NotBlank
    private String agentId;

    @NotBlank
    private String taskCron;

    private String taskDesc;

    @NotBlank
    private String taskParam;

    @Builder.Default
    private Integer taskStatus = 1;
}
