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
public class AdvisorVO {
    private Long id;
    private String advisorId;
    private String advisorName;
    private String advisorType;
    private String advisorDesc;
    private Integer advisorOrder;
    private String advisorParam;
    private LocalDateTime updateTime;
}
