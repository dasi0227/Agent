package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.types.dto.response.admin.DashboardResponse;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.query.*;
import com.dasi.types.dto.result.PageResult;

import java.util.List;
import java.util.Map;

public interface IAdminService {

    // Api
    PageResult<ApiVO> apiPage(ApiPageRequest request);
    void apiInsert(ApiManageRequest request);
    void apiUpdate(ApiManageRequest request);
    void apiDelete(Long id);

    // Model
    PageResult<ModelVO> modelPage(ModelPageRequest request);
    void modelInsert(ModelManageRequest request);
    void modelUpdate(ModelManageRequest request);
    void modelDelete(Long id);

    // Mcp
    PageResult<McpVO> mcpPage(McpPageRequest request);
    void mcpInsert(McpManageRequest request);
    void mcpUpdate(McpManageRequest request);
    void mcpDelete(Long id);

    // Advisor
    PageResult<AdvisorVO> advisorPage(AdvisorPageRequest request);
    void advisorInsert(AdvisorManageRequest request);
    void advisorUpdate(AdvisorManageRequest request);
    void advisorDelete(Long id);

    // Prompt
    PageResult<PromptVO> promptPage(PromptPageRequest request);
    void promptInsert(PromptManageRequest request);
    void promptUpdate(PromptManageRequest request);
    void promptDelete(Long id);

    // Client
    PageResult<ClientVO> clientPage(ClientPageRequest request);
    void clientInsert(ClientManageRequest request);
    void clientUpdate(ClientManageRequest request);
    void clientDelete(Long id);
    void clientToggle(Long id, Integer status);

    // Agent
    PageResult<AgentVO> agentPage(AgentPageRequest request);
    List<AgentVO> agentList(AgentListRequest request);
    void agentInsert(AgentManageRequest request);
    void agentUpdate(AgentManageRequest request);
    void agentDelete(Long id);
    void agentToggle(Long id, Integer status);

    // User
    PageResult<UserVO> userPage(UserPageRequest request);
    void userInsert(UserManageRequest request);
    void userUpdate(UserManageRequest request);
    void userDelete(Long id);
    void userToggle(Long id, Integer status);

    // Config
    Map<String, List<ConfigVO>> configList(ConfigListRequest request);
    void configInsert(ConfigManageRequest request);
    void configUpdate(ConfigManageRequest request);
    void configDelete(Long id);
    void configToggle(Long id, Integer configStatus);

    // Flow
    List<ClientDetailVO> flowClient();
    List<FlowVO> flowAgent(String agentId);
    void flowInsert(FlowManageRequest request);
    void flowUpdate(FlowManageRequest request);
    void flowDelete(Long id);

    // Task
    PageResult<TaskVO> taskPage(TaskPageRequest request);
    void taskInsert(TaskManageRequest request);
    void taskUpdate(TaskManageRequest request);
    void taskDelete(Long id);
    void taskToggle(Long id, Integer status);

    // Session
    List<SessionVO> listSession();

    // List
    List<String> listClientType();
    List<String> listAgentType();
    List<String> listClientRole();
    List<String> listUserRole();
    List<String> listApiId();
    List<String> listModelId();
    List<String> listConfigType();

    // Dashboard
    DashboardResponse dashboard();
}
