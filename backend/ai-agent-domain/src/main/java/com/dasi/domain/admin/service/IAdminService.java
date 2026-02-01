package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.page.*;
import com.dasi.types.dto.result.PageResult;

public interface IAdminService {

    // Api
    PageResult<ApiVO> apiPage(ApiPageRequest request);
    void apiInsert(ApiManageRequest request);
    void apiUpdate(ApiManageRequest request);
    void apiDelete(Long id);
    void apiToggle(Long id, Integer status);

    // Model
    PageResult<ModelVO> modelPage(ModelPageRequest request);
    void modelInsert(ModelManageRequest request);
    void modelUpdate(ModelManageRequest request);
    void modelDelete(Long id);
    void modelToggle(Long id, Integer status);

    // Mcp
    PageResult<McpVO> mcpPage(McpPageRequest request);
    void mcpInsert(McpManageRequest request);
    void mcpUpdate(McpManageRequest request);
    void mcpDelete(Long id);
    void mcpToggle(Long id, Integer status);

    // Advisor
    PageResult<AdvisorVO> advisorPage(AdvisorPageRequest request);
    void advisorInsert(AdvisorManageRequest request);
    void advisorUpdate(AdvisorManageRequest request);
    void advisorDelete(Long id);
    void advisorToggle(Long id, Integer status);

    // Prompt
    PageResult<PromptVO> promptPage(PromptPageRequest request);
    void promptInsert(PromptManageRequest request);
    void promptUpdate(PromptManageRequest request);
    void promptDelete(Long id);
    void promptToggle(Long id, Integer status);

    // Client
    PageResult<ClientVO> clientPage(ClientPageRequest request);
    void clientInsert(ClientManageRequest request);
    void clientUpdate(ClientManageRequest request);
    void clientDelete(Long id);
    void clientToggle(Long id, Integer status);

    // Agent
    PageResult<AdminAgentVO> agentPage(AgentPageRequest request);
    void agentInsert(AgentManageRequest request);
    void agentUpdate(AgentManageRequest request);
    void agentDelete(Long id);
    void agentToggle(Long id, Integer status);

    // User
    PageResult<UserVO> userPage(UserPageRequest request);
    void userInsert(UserManageRequest request);
    void userUpdate(UserManageRequest request);
    void userDelete(Long id);

}
