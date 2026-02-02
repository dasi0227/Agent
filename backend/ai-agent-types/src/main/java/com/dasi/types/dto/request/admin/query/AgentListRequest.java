package com.dasi.types.dto.request.admin.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentListRequest {

    private String idKeyword;

    private String nameKeyword;

    private String agentType;

}
