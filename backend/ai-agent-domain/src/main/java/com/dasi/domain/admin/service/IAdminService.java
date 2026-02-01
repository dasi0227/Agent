package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.types.dto.request.admin.*;
import com.dasi.types.dto.result.PageResult;

public interface IAdminService {

    // Api
    PageResult<ApiVO> apiPage(ApiPageRequest request);
    void apiInsert(ApiManageRequest request);
    void apiUpdate(ApiManageRequest request);
    void apiDelete(String apiId);
    void apiToggle(String apiId, Integer status);

    // Model
    PageResult<ModelVO> modelPage(ModelPageRequest request);
    void modelInsert(ModelManageRequest request);
    void modelUpdate(ModelManageRequest request);
    void modelDelete(String modelId);
    void modelToggle(String modelId, Integer status);

    // Mcp
    PageResult<McpVO> mcpPage(McpPageRequest request);
    void mcpInsert(McpManageRequest request);
    void mcpUpdate(McpManageRequest request);
    void mcpDelete(String mcpId);
    void mcpToggle(String mcpId, Integer status);

    // Advisor
    PageResult<AdvisorVO> advisorPage(AdvisorPageRequest request);
    void advisorInsert(AdvisorManageRequest request);
    void advisorUpdate(AdvisorManageRequest request);
    void advisorDelete(String advisorId);
    void advisorToggle(String advisorId, Integer status);

    // Prompt
    PageResult<PromptVO> promptPage(PromptPageRequest request);
    void promptInsert(PromptManageRequest request);
    void promptUpdate(PromptManageRequest request);
    void promptDelete(String promptId);
    void promptToggle(String promptId, Integer status);

    // Client
    PageResult<ClientVO> clientPage(ClientPageRequest request);
    void clientInsert(ClientManageRequest request);
    void clientUpdate(ClientManageRequest request);
    void clientDelete(String clientId);
    void clientToggle(String clientId, Integer status);

    // Agent
    PageResult<AdminAgentVO> agentPage(AgentPageRequest request);
    void agentInsert(AgentManageRequest request);
    void agentUpdate(AgentManageRequest request);
    void agentDelete(String agentId);
    void agentToggle(String agentId, Integer status);

    // User
    PageResult<UserVO> userPage(UserPageRequest request);
    void userInsert(UserManageRequest request);
    void userUpdate(UserManageRequest request);
    void userDelete(String userId);

}
