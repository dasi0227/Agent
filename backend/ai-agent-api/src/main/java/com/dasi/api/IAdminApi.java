package com.dasi.api;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.query.*;
import com.dasi.types.dto.response.admin.DashboardResponse;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.dto.result.Result;

import java.util.List;
import java.util.Map;

public interface IAdminApi {

    Result<DashboardResponse> dashboard();

    Result<PageResult<ApiVO>> apiPage(ApiPageRequest request);

    Result<Void> apiInsert(ApiManageRequest request);

    Result<Void> apiUpdate(ApiManageRequest request);

    Result<Void> apiDelete(Long id);

    Result<PageResult<ModelVO>> modelPage(ModelPageRequest request);

    Result<Void> modelInsert(ModelManageRequest request);

    Result<Void> modelUpdate(ModelManageRequest request);

    Result<Void> modelDelete(Long id);

    Result<PageResult<McpVO>> mcpPage(McpPageRequest request);

    Result<Void> mcpInsert(McpManageRequest request);

    Result<Void> mcpUpdate(McpManageRequest request);

    Result<Void> mcpDelete(Long id);

    Result<PageResult<AdvisorVO>> advisorPage(AdvisorPageRequest request);

    Result<Void> advisorInsert(AdvisorManageRequest request);

    Result<Void> advisorUpdate(AdvisorManageRequest request);

    Result<Void> advisorDelete(Long id);

    Result<PageResult<PromptVO>> promptPage(PromptPageRequest request);

    Result<Void> promptInsert(PromptManageRequest request);

    Result<Void> promptUpdate(PromptManageRequest request);

    Result<Void> promptDelete(Long id);

    Result<PageResult<ClientVO>> clientPage(ClientPageRequest request);

    Result<Void> clientInsert(ClientManageRequest request);

    Result<Void> clientUpdate(ClientManageRequest request);

    Result<Void> clientDelete(Long id);

    Result<Void> clientToggle(Long id, Integer clientStatus);

    Result<PageResult<AgentVO>> agentPage(AgentPageRequest request);

    Result<List<AgentVO>> agentList(AgentListRequest request);

    Result<Void> agentInsert(AgentManageRequest request);

    Result<Void> agentUpdate(AgentManageRequest request);

    Result<Void> agentDelete(Long id);

    Result<Void> agentToggle(Long id, Integer agentStatus);

    Result<PageResult<UserVO>> userPage(UserPageRequest request);

    Result<Void> userInsert(UserManageRequest request);

    Result<Void> userUpdate(UserManageRequest request);

    Result<Void> userDelete(Long id);

    Result<Void> userToggle(Long id, Integer userStatus);

    Result<Map<String, List<ConfigVO>>> configList(ConfigListRequest request);

    Result<Void> configInsert(ConfigManageRequest request);

    Result<Void> configUpdate(ConfigManageRequest request);

    Result<Void> configDelete(Long id);

    Result<Void> configToggle(Long id, Integer configStatus);

    Result<List<ClientDetailVO>> flowClient();

    Result<List<FlowVO>> flowAgent(String agentId);

    Result<Void> flowInsert(FlowManageRequest request);

    Result<Void> flowUpdate(FlowManageRequest request);

    Result<Void> flowDelete(Long id);

    Result<PageResult<TaskVO>> taskPage(TaskPageRequest request);

    Result<Void> taskInsert(TaskManageRequest request);

    Result<Void> taskUpdate(TaskManageRequest request);

    Result<Void> taskDelete(Long id);

    Result<Void> taskToggle(Long id, Integer taskStatus);

    Result<List<SessionVO>> listSession();

    Result<List<String>> listClientType();

    Result<List<String>> listAgentType();

    Result<List<String>> listConfigType();

    Result<List<String>> listClientRole();

    Result<List<String>> listUserRole();

    Result<List<String>> listApiId();

    Result<List<String>> listModelId();
}
