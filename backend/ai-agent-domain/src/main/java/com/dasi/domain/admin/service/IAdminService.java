package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.command.*;
import com.dasi.domain.admin.model.query.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.types.dto.request.admin.ApiManageRequest;
import com.dasi.types.dto.request.admin.ApiPageRequest;
import com.dasi.types.dto.result.PageResult;

public interface IAdminService {

    // Api
    PageResult<ApiVO> apiPage(ApiPageRequest apiPageRequest);

    void apiInsert(ApiManageRequest apiManageRequest);

    void apiUpdate(ApiManageRequest apiManageRequest);

    void apiDelete(String apiId);

    void apiToggle(String apiId, Integer status);

    // Model
    PageResult<ModelVO> pageModel(ModelQuery query);

    ModelVO createModel(ModelCommand command);

    ModelVO updateModel(ModelCommand command);

    void deleteModel(Long id);

    ModelVO switchModelStatus(Long id, Integer status);

    // MCP
    PageResult<McpVO> pageMcp(McpQuery query);

    McpVO createMcp(McpCommand command);

    McpVO updateMcp(McpCommand command);

    void deleteMcp(Long id);

    McpVO switchMcpStatus(Long id, Integer status);

    // Advisor
    PageResult<AdvisorVO> pageAdvisor(AdvisorQuery query);

    AdvisorVO createAdvisor(AdvisorCommand command);

    AdvisorVO updateAdvisor(AdvisorCommand command);

    void deleteAdvisor(Long id);

    AdvisorVO switchAdvisorStatus(Long id, Integer status);

    // Prompt
    PageResult<PromptVO> pagePrompt(PromptQuery query);

    PromptVO createPrompt(PromptCommand command);

    PromptVO updatePrompt(PromptCommand command);

    void deletePrompt(Long id);

    PromptVO switchPromptStatus(Long id, Integer status);

    // Client
    PageResult<ClientVO> pageClient(ClientQuery query);

    ClientVO createClient(ClientCommand command);

    ClientVO updateClient(ClientCommand command);

    void deleteClient(Long id);

    ClientVO switchClientStatus(Long id, Integer status);

    // Flow
    PageResult<FlowVO> pageFlow(FlowQuery query);

    FlowVO createFlow(FlowCommand command);

    FlowVO updateFlow(FlowCommand command);

    void deleteFlow(Long id);

    FlowVO switchFlowStatus(Long id, Integer status);

    PageResult<AdminAgentVO> pageAgent(AgentQuery query);

    AdminAgentVO createAgent(AgentCommand command);

    AdminAgentVO updateAgent(AgentCommand command);

    void deleteAgent(Long id);

    AdminAgentVO switchAgentStatus(Long id, Integer status);

    PageResult<UserAdminVO> pageUser(UserQuery query);

    UserAdminVO createUser(UserCommand command);

    UserAdminVO updateUser(UserCommand command);

    void deleteUser(Long id);

}
