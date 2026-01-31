package com.dasi.domain.admin.repository;

import com.dasi.domain.admin.model.*;
import com.dasi.domain.admin.model.vo.ApiVO;
import com.dasi.domain.login.model.User;
import com.dasi.types.dto.request.admin.ApiManageRequest;
import com.dasi.types.dto.request.admin.ApiPageRequest;

import java.util.List;

public interface IAdminRepository {

    // Api
    List<ApiVO> apiPage(ApiPageRequest apiPageRequest);

    Integer apiCount(ApiPageRequest apiPageRequest);

    ApiVO apiQuery(String apiId);

    void apiInsert(ApiManageRequest apiManageRequest);

    void apiUpdate(ApiManageRequest apiManageRequest);

    void apiDelete(String apiId);

    void apiToggle(String apiId, Integer status);

    List<String> queryModelIdListByApiId(String apiId);

    // Model
    List<AdminModel> queryModelList(String keyword, String apiId, Integer status, int offset, int size);

    Long countModel(String keyword, String apiId, Integer status);

    AdminModel queryModelById(Long id);

    AdminModel queryModelByModelId(String modelId);

    void insertModel(AdminModel model);

    void updateModel(AdminModel model);

    void deleteModel(Long id);

    List<String> queryClientIdListByModelId(String modelId);

    // Mcp
    List<AdminMcp> queryMcpList(String keyword, String mcpType, Integer status, int offset, int size);

    Long countMcp(String keyword, String mcpType, Integer status);

    AdminMcp queryMcpById(Long id);

    AdminMcp queryMcpByMcpId(String mcpId);

    void insertMcp(AdminMcp mcp);

    void updateMcp(AdminMcp mcp);

    void deleteMcp(Long id);

    List<String> queryClientIdListByMcpId(String mcpId);

    // Advisor
    List<AdminAdvisor> queryAdvisorList(String keyword, String advisorType, Integer status, int offset, int size);

    Long countAdvisor(String keyword, String advisorType, Integer status);

    AdminAdvisor queryAdvisorById(Long id);

    AdminAdvisor queryAdvisorByAdvisorId(String advisorId);

    void insertAdvisor(AdminAdvisor advisor);

    void updateAdvisor(AdminAdvisor advisor);

    void deleteAdvisor(Long id);

    List<String> queryClientIdListByAdvisorId(String advisorId);

    // Prompt
    List<AdminPrompt> queryPromptList(String keyword, Integer status, int offset, int size);

    Long countPrompt(String keyword, Integer status);

    AdminPrompt queryPromptById(Long id);

    AdminPrompt queryPromptByPromptId(String promptId);

    void insertPrompt(AdminPrompt prompt);

    void updatePrompt(AdminPrompt prompt);

    void deletePrompt(Long id);

    List<String> queryClientIdListByPromptId(String promptId);

    // Client
    List<AdminClient> queryClientList(String keyword, String modelId, String clientType, Integer status, int offset, int size);

    Long countClient(String keyword, String modelId, String clientType, Integer status);

    AdminClient queryClientById(Long id);

    AdminClient queryClientByClientId(String clientId);

    void insertClient(AdminClient client);

    void updateClient(AdminClient client);

    void deleteClient(Long id);

    List<AdminFlow> queryFlowListByClientId(String clientId);

    List<String> queryConfigClientIdListByConfig(String configType, String configValue);

    // Flow
    List<AdminFlow> queryFlowList(String agentId, String clientId, Integer status, int offset, int size);

    Long countFlow(String agentId, String clientId, Integer status);

    AdminFlow queryFlowById(Long id);

    void insertFlow(AdminFlow flow);

    void updateFlow(AdminFlow flow);

    void deleteFlow(Long id);

    void updateFlowStatus(Long id, Integer status);

    // Agent
    List<AdminAgent> queryAgentList(String keyword, Integer status, String agentType, int offset, int size);

    Long countAgent(String keyword, Integer status, String agentType);

    AdminAgent apiQuery(Long id);

    AdminAgent queryAgentByAgentId(String agentId);

    void insertAgent(AdminAgent agent);

    void updateAgent(AdminAgent agent);

    void deleteAgent(Long id);

    List<AdminFlow> queryFlowListByAgentId(String agentId);

    // User
    List<User> queryUserList(String username, String role, int offset, int size);

    Long countUser(String username, String role);

    User queryUserById(Long id);

    User queryUserByUsername(String username);

    void insertUser(User user);

    void updateUser(User user);

    void deleteUser(Long id);

}
