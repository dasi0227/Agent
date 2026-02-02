package com.dasi.domain.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDetailVO {

    private String clientId;

    private String clientRole;

    private ClientVO client;

    private ModelVO model;

    private ApiVO api;

    private List<McpVO> mcpList;

    private List<AdvisorVO> advisorList;

    private List<PromptVO> promptList;

}
