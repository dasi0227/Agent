package com.dasi.domain.admin.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentQuery {

    private String keyword;

    private Integer agentStatus;

    private String agentType;

    private Integer page;

    private Integer size;
}
