package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.types.dto.request.admin.*;
import com.dasi.types.dto.result.PageResult;

public interface IAdminService {

    // Api
    PageResult<ApiVO> apiPage(ApiPageRequest apiPageRequest);

    void apiInsert(ApiManageRequest apiManageRequest);

    void apiUpdate(ApiManageRequest apiManageRequest);

    void apiDelete(String apiId);

    void apiToggle(String apiId, Integer status);

    // Model
    PageResult<ModelVO> modelPage(ModelPageRequest request);

    ModelVO modelCreate(ModelManageRequest request);

    ModelVO modelUpdate(ModelManageRequest request);

    void modelDelete(String modelId);

    ModelVO modelToggle(String modelId, Integer status);

    // Mcp
    PageResult<McpVO> mcpPage(McpPageRequest request);

    McpVO mcpCreate(McpManageRequest request);

    McpVO mcpUpdate(McpManageRequest request);

    void mcpDelete(String mcpId);

    McpVO mcpToggle(String mcpId, Integer status);

    // Advisor
    PageResult<AdvisorVO> advisorPage(AdvisorPageRequest request);

    AdvisorVO advisorCreate(AdvisorManageRequest request);

    AdvisorVO advisorUpdate(AdvisorManageRequest request);

    void advisorDelete(String advisorId);

    AdvisorVO advisorToggle(String advisorId, Integer status);

    // Prompt
    PageResult<PromptVO> promptPage(PromptPageRequest request);

    PromptVO promptCreate(PromptManageRequest request);

    PromptVO promptUpdate(PromptManageRequest request);

    void promptDelete(String promptId);

    PromptVO promptToggle(String promptId, Integer status);

    // Client
    PageResult<ClientVO> clientPage(ClientPageRequest request);

    ClientVO clientCreate(ClientManageRequest request);

    ClientVO clientUpdate(ClientManageRequest request);

    void clientDelete(String clientId);

    ClientVO clientToggle(String clientId, Integer status);

    // Flow
    PageResult<FlowVO> flowPage(FlowPageRequest request);

    FlowVO flowCreate(FlowManageRequest request);

    FlowVO flowUpdate(FlowManageRequest request);

    void flowDelete(String flowId);

    FlowVO flowToggle(String flowId, Integer status);

    // Agent
    PageResult<AdminAgentVO> agentPage(AgentPageRequest request);

    AdminAgentVO agentCreate(AgentManageRequest request);

    AdminAgentVO agentUpdate(AgentManageRequest request);

    void agentDelete(String agentId);

    AdminAgentVO agentToggle(String agentId, Integer status);

    // User
    PageResult<UserAdminVO> userPage(UserPageRequest request);

    UserAdminVO userCreate(UserManageRequest request);

    UserAdminVO userUpdate(UserManageRequest request);

    void userDelete(String userId);

}
