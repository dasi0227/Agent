package com.dasi.domain.admin.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorCommand {

    private Long id;

    private String advisorId;

    private String advisorName;

    private String advisorType;

    private String advisorDesc;

    private Integer advisorOrder;

    private String advisorParam;

    private Integer advisorStatus;
}
