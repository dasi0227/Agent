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
public class AdminAdvisor {

    private Long id;

    private String advisorId;

    private String advisorName;

    private String advisorType;

    private String advisorDesc;

    private Integer advisorOrder;

    private String advisorParam;

    private Integer advisorStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
