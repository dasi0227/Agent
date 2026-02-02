package com.dasi.domain.admin.repository;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.types.dto.request.admin.page.ConfigListRequest;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.page.*;

import java.util.List;

public interface IAdminRepository {

    // Api
    List<ApiVO> apiPage(ApiPageRequest apiPageRequest);
    Integer apiCount(ApiPageRequest apiPageRequest);
    ApiVO apiQuery(Long id);
    ApiVO apiQuery(String apiId);
    void apiInsert(ApiManageRequest apiManageRequest);
    void apiUpdate(ApiManageRequest apiManageRequest);
    void apiDelete(Long id);
    void apiToggle(Long id, Integer status);

    // Model
    List<ModelVO> modelPage(ModelPageRequest request);
    Integer modelCount(ModelPageRequest request);
    ModelVO modelQuery(Long id);
    ModelVO modelQuery(String modelId);
    void modelInsert(ModelManageRequest request);
    void modelUpdate(ModelManageRequest request);
    void modelDelete(Long id);
    void modelToggle(Long id, Integer status);

    // Mcp
    List<McpVO> mcpPage(McpPageRequest request);
    Integer mcpCount(McpPageRequest request);
    McpVO mcpQuery(Long id);
    McpVO mcpQuery(String mcpId);
    void mcpInsert(McpManageRequest request);
    void mcpUpdate(McpManageRequest request);
    void mcpDelete(Long id);
    void mcpToggle(Long id, Integer status);

    // Advisor
    List<AdvisorVO> advisorPage(AdvisorPageRequest request);
    Integer advisorCount(AdvisorPageRequest request);
    AdvisorVO advisorQuery(Long id);
    AdvisorVO advisorQuery(String advisorId);
    void advisorInsert(AdvisorManageRequest request);
    void advisorUpdate(AdvisorManageRequest request);
    void advisorDelete(Long id);
    void advisorToggle(Long id, Integer status);

    // Prompt
    List<PromptVO> promptPage(PromptPageRequest request);
    Integer promptCount(PromptPageRequest request);
    PromptVO promptQuery(Long id);
    PromptVO promptQuery(String promptId);
    void promptInsert(PromptManageRequest request);
    void promptUpdate(PromptManageRequest request);
    void promptDelete(Long id);
    void promptToggle(Long id, Integer status);

    // Client
    List<ClientVO> clientPage(ClientPageRequest request);
    Integer clientCount(ClientPageRequest request);
    ClientVO clientQuery(Long id);
    ClientVO clientQuery(String clientId);
    void clientInsert(ClientManageRequest request);
    void clientUpdate(ClientManageRequest request);
    void clientDelete(Long id);
    void clientToggle(Long id, Integer status);

    // Agent
    List<AdminAgentVO> agentPage(AgentPageRequest request);
    Integer agentCount(AgentPageRequest request);
    AdminAgentVO agentQuery(Long id);
    AdminAgentVO agentQuery(String agentId);
    void agentInsert(AgentManageRequest request);
    void agentUpdate(AgentManageRequest request);
    void agentDelete(Long id);
    void agentToggle(Long id, Integer status);

    // User
    List<UserVO> userPage(UserPageRequest request);
    Integer userCount(UserPageRequest request);
    UserVO userQuery(Long id);
    UserVO userQuery(String username);
    void userInsert(UserManageRequest request);
    void userUpdate(UserManageRequest request);
    void userDelete(Long id);

    // Config
    List<ConfigVO> configList(ConfigListRequest request);
    ConfigVO configQuery(ConfigManageRequest request);
    ConfigVO configQuery(Long id);
    void configInsert(ConfigManageRequest request);
    void configUpdate(ConfigManageRequest request);
    void configDelete(Long id);
    void configToggle(Long id, Integer status);

    // Depend
    List<String> queryClientDependOnPrompt(String promptId);
    List<String> queryClientDependOnAdvisor(String advisorId);
    List<String> queryClientDependOnMcp(String mcpId);
    List<String> queryModelDependOnApi(String apiId);
    List<String> queryClientDependOnModel(String modelId);
    List<String> queryAgentDependOnClient(String clientId);

    // List
    List<String> listApiId();
    List<String> listModelId();

}
