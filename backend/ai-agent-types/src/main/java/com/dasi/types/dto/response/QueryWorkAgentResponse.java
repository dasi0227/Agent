package com.dasi.types.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryWorkAgentResponse {

    private String agentId;

    private String agentName;

    private String agentDesc;

}
