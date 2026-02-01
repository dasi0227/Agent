package com.dasi.infrastructure.repository;

import com.dasi.domain.admin.model.*;
import com.dasi.domain.admin.model.vo.ApiVO;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.domain.login.model.User;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.types.dto.request.admin.ApiManageRequest;
import com.dasi.types.dto.request.admin.ApiPageRequest;
import com.dasi.types.dto.request.admin.AgentManageRequest;
import com.dasi.types.dto.request.admin.AdvisorManageRequest;
import com.dasi.types.dto.request.admin.ClientManageRequest;
import com.dasi.types.dto.request.admin.FlowManageRequest;
import com.dasi.types.dto.request.admin.McpManageRequest;
import com.dasi.types.dto.request.admin.ModelManageRequest;
import com.dasi.types.dto.request.admin.PromptManageRequest;
import com.dasi.types.dto.request.admin.UserManageRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.ArrayList;

@Repository
public class AdminRepository implements IAdminRepository {

    @Resource
    private IAiApiDao aiApiDao;

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private IAiMcpDao aiMcpDao;

    @Resource
    private IAiAdvisorDao aiAdvisorDao;

    @Resource
    private IAiPromptDao aiPromptDao;

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiConfigDao aiConfigDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IUserDao userDao;

    // -------------------- API --------------------
    @Override
    public List<ApiVO> apiPage(ApiPageRequest apiPageRequest) {

        String idKeyword = apiPageRequest.getIdKeyword();
        Integer pageNum = apiPageRequest.getPageNum();
        Integer pageSize = apiPageRequest.getPageSize();

        Integer offset = (pageNum - 1) * pageSize;
        List<AiApi> poList = aiApiDao.page(idKeyword, offset, pageSize);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }

        return poList.stream().map(this::toApiVO).toList();
    }

    @Override
    public Integer apiCount(ApiPageRequest apiPageRequest) {
        return aiApiDao.count(apiPageRequest.getIdKeyword());
    }

    @Override
    public ApiVO apiQuery(String apiId) {
        AiApi aiApi = aiApiDao.query(apiId);
        return toApiVO(aiApi);
    }

    @Override
    public void apiInsert(ApiManageRequest apiManageRequest) {
        AiApi aiApi = toApiPO(apiManageRequest);
        aiApiDao.insert(aiApi);
    }

    @Override
    public void apiUpdate(ApiManageRequest apiManageRequest) {
        AiApi aiApi = toApiPO(apiManageRequest);
        aiApiDao.update(aiApi);
    }

    @Override
    public void apiDelete(String apiId) {
        aiApiDao.delete(apiId);
    }

    @Override
    public void apiToggle(String apiId, Integer apiStatus) {
        AiApi aiApi = AiApi.builder().apiId(apiId).apiStatus(apiStatus).build();
        aiApiDao.toggle(aiApi);
    }

    @Override
    public List<String> queryModelIdListByApiId(String apiId) {
        return aiModelDao.queryModelIdListByApiId(apiId);
    }

    private ApiVO toApiVO(AiApi aiApi) {
        return ApiVO.builder()
                .apiId(aiApi.getApiId())
                .apiBaseUrl(aiApi.getApiBaseUrl())
                .apiKey(aiApi.getApiKey())
                .apiCompletionsPath(aiApi.getApiCompletionsPath())
                .apiEmbeddingsPath(aiApi.getApiEmbeddingsPath())
                .apiStatus(aiApi.getApiStatus())
                .updateTime(aiApi.getUpdateTime())
                .build();
    }

    private AiApi toApiPO(ApiManageRequest apiManageRequest) {
        return AiApi.builder()
                .apiId(apiManageRequest.getApiId())
                .apiBaseUrl(apiManageRequest.getApiBaseUrl())
                .apiKey(apiManageRequest.getApiKey())
                .apiCompletionsPath(apiManageRequest.getApiCompletionsPath())
                .apiEmbeddingsPath(apiManageRequest.getApiEmbeddingsPath())
                .apiStatus(apiManageRequest.getApiStatus())
                .build();
    }

    // -------------------- Model --------------------
    @Override
    public List<AdminModel> queryModelList(String keyword, String apiId, Integer status, int offset, int size) {
        List<AiModel> poList = aiModelDao.queryPage(keyword, apiId, status, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminModel).toList();
    }

    @Override
    public Long countModel(String keyword, String apiId, Integer status) {
        return aiModelDao.count(keyword, apiId, status);
    }

    @Override
    public AdminModel queryModelById(Long id) {
        return toAdminModel(aiModelDao.queryById(id));
    }

    @Override
    public AdminModel queryModelByModelId(String modelId) {
        return toAdminModel(aiModelDao.queryByModelId(modelId));
    }

    @Override
    public void insertModel(ModelManageRequest request) {
        AiModel po = toModelPo(request);
        aiModelDao.insert(po);
    }

    @Override
    public void updateModel(ModelManageRequest request) {
        aiModelDao.update(toModelPo(request));
    }

    @Override
    public void deleteModel(Long id) {
        aiModelDao.delete(id);
    }

    @Override
    public List<String> queryClientIdListByModelId(String modelId) {
        return aiClientDao.queryClientIdListByModelId(modelId);
    }

    // -------------------- MCP --------------------
    @Override
    public List<AdminMcp> queryMcpList(String keyword, String mcpType, Integer status, int offset, int size) {
        List<AiMcp> poList = aiMcpDao.queryPage(keyword, mcpType, status, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminMcp).toList();
    }

    @Override
    public Long countMcp(String keyword, String mcpType, Integer status) {
        return aiMcpDao.count(keyword, mcpType, status);
    }

    @Override
    public AdminMcp queryMcpById(Long id) {
        return toAdminMcp(aiMcpDao.queryById(id));
    }

    @Override
    public AdminMcp queryMcpByMcpId(String mcpId) {
        return toAdminMcp(aiMcpDao.queryByMcpId(mcpId));
    }

    @Override
    public void insertMcp(McpManageRequest request) {
        AiMcp po = toMcpPo(request);
        aiMcpDao.insert(po);
    }

    @Override
    public void updateMcp(McpManageRequest request) {
        aiMcpDao.update(toMcpPo(request));
    }

    @Override
    public void deleteMcp(Long id) {
        aiMcpDao.delete(id);
    }

    @Override
    public List<String> queryClientIdListByMcpId(String mcpId) {
        return aiConfigDao.queryClientIdListByConfig("mcp", mcpId);
    }

    // -------------------- Advisor --------------------
    @Override
    public List<AdminAdvisor> queryAdvisorList(String keyword, String advisorType, Integer status, int offset, int size) {
        List<AiAdvisor> poList = aiAdvisorDao.queryPage(keyword, advisorType, status, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminAdvisor).toList();
    }

    @Override
    public Long countAdvisor(String keyword, String advisorType, Integer status) {
        return aiAdvisorDao.count(keyword, advisorType, status);
    }

    @Override
    public AdminAdvisor queryAdvisorById(Long id) {
        return toAdminAdvisor(aiAdvisorDao.queryById(id));
    }

    @Override
    public AdminAdvisor queryAdvisorByAdvisorId(String advisorId) {
        return toAdminAdvisor(aiAdvisorDao.queryByAdvisorId(advisorId));
    }

    @Override
    public void insertAdvisor(AdvisorManageRequest request) {
        AiAdvisor po = toAdvisorPo(request);
        aiAdvisorDao.insert(po);
    }

    @Override
    public void updateAdvisor(AdvisorManageRequest request) {
        aiAdvisorDao.update(toAdvisorPo(request));
    }

    @Override
    public void deleteAdvisor(Long id) {
        aiAdvisorDao.delete(id);
    }

    @Override
    public List<String> queryClientIdListByAdvisorId(String advisorId) {
        return aiConfigDao.queryClientIdListByConfig("advisor", advisorId);
    }

    // -------------------- Prompt --------------------
    @Override
    public List<AdminPrompt> queryPromptList(String keyword, Integer status, int offset, int size) {
        List<AiPrompt> poList = aiPromptDao.queryPage(keyword, status, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminPrompt).toList();
    }

    @Override
    public Long countPrompt(String keyword, Integer status) {
        return aiPromptDao.count(keyword, status);
    }

    @Override
    public AdminPrompt queryPromptById(Long id) {
        return toAdminPrompt(aiPromptDao.queryById(id));
    }

    @Override
    public AdminPrompt queryPromptByPromptId(String promptId) {
        return toAdminPrompt(aiPromptDao.queryByPromptId(promptId));
    }

    @Override
    public void insertPrompt(PromptManageRequest request) {
        AiPrompt po = toPromptPo(request);
        aiPromptDao.insert(po);
    }

    @Override
    public void updatePrompt(PromptManageRequest request) {
        aiPromptDao.update(toPromptPo(request));
    }

    @Override
    public void deletePrompt(Long id) {
        aiPromptDao.delete(id);
    }

    @Override
    public List<String> queryClientIdListByPromptId(String promptId) {
        return aiConfigDao.queryClientIdListByConfig("prompt", promptId);
    }

    // -------------------- Client --------------------
    @Override
    public List<AdminClient> queryClientList(String keyword, String modelId, String clientType, Integer status, int offset, int size) {
        List<AiClient> poList = aiClientDao.queryPage(keyword, modelId, clientType, status, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminClient).toList();
    }

    @Override
    public Long countClient(String keyword, String modelId, String clientType, Integer status) {
        return aiClientDao.count(keyword, modelId, clientType, status);
    }

    @Override
    public AdminClient queryClientById(Long id) {
        return toAdminClient(aiClientDao.queryById(id));
    }

    @Override
    public AdminClient queryClientByClientId(String clientId) {
        return toAdminClient(aiClientDao.queryByClientId(clientId));
    }

    @Override
    public void insertClient(ClientManageRequest request) {
        AiClient po = toClientPo(request);
        aiClientDao.insert(po);
    }

    @Override
    public void updateClient(ClientManageRequest request) {
        aiClientDao.update(toClientPo(request));
    }

    @Override
    public void deleteClient(Long id) {
        aiClientDao.delete(id);
    }

    @Override
    public List<AdminFlow> queryFlowListByClientId(String clientId) {
        List<AiFlow> poList = aiFlowDao.queryByClientId(clientId);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminFlow).toList();
    }

    @Override
    public List<String> queryConfigClientIdListByConfig(String configType, String configValue) {
        return aiConfigDao.queryClientIdListByConfig(configType, configValue);
    }

    // -------------------- Flow --------------------
    @Override
    public List<AdminFlow> queryFlowList(String agentId, String clientId, Integer status, int offset, int size) {
        List<AiFlow> poList = aiFlowDao.queryPage(agentId, clientId, status, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminFlow).toList();
    }

    @Override
    public Long countFlow(String agentId, String clientId, Integer status) {
        return aiFlowDao.count(agentId, clientId, status);
    }

    @Override
    public AdminFlow queryFlowById(Long id) {
        return toAdminFlow(aiFlowDao.queryById(id));
    }

    @Override
    public void insertFlow(FlowManageRequest request) {
        AiFlow po = toFlowPo(request);
        aiFlowDao.insert(po);
    }

    @Override
    public void updateFlow(FlowManageRequest request) {
        aiFlowDao.update(toFlowPo(request));
    }

    @Override
    public void deleteFlow(Long id) {
        aiFlowDao.delete(id);
    }

    @Override
    public void updateFlowStatus(Long id, Integer status) {
        aiFlowDao.updateStatus(id, status);
    }

    // -------------------- Agent --------------------
    @Override
    public List<AdminAgent> queryAgentList(String keyword, Integer status, String agentType, int offset, int size) {
        List<AiAgent> poList = aiAgentDao.queryAgentPage(keyword, status, agentType, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminAgent).toList();
    }

    @Override
    public Long countAgent(String keyword, Integer status, String agentType) {
        return aiAgentDao.countAgent(keyword, status, agentType);
    }

    @Override
    public AdminAgent apiQuery(Long id) {
        AiAgent po = aiAgentDao.queryAgentById(id);
        return toAdminAgent(po);
    }

    @Override
    public AdminAgent queryAgentByAgentId(String agentId) {
        AiAgent po = aiAgentDao.queryAgentByAgentId(agentId);
        return toAdminAgent(po);
    }

    @Override
    public void insertAgent(AgentManageRequest request) {
        AiAgent po = toAgentPo(request);
        aiAgentDao.insertAgent(po);
    }

    @Override
    public void updateAgent(AgentManageRequest request) {
        aiAgentDao.updateAgent(toAgentPo(request));
    }

    @Override
    public void deleteAgent(Long id) {
        aiAgentDao.deleteAgent(id);
    }

    @Override
    public List<AdminFlow> queryFlowListByAgentId(String agentId) {
        List<AiFlow> poList = aiFlowDao.queryByAgentId(agentId);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminFlow).toList();
    }

    // -------------------- User --------------------
    @Override
    public List<User> queryUserList(String username, String role, int offset, int size) {
        List<com.dasi.infrastructure.persistent.po.User> poList = userDao.queryList(username, role, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return new ArrayList<>();
        }
        return poList.stream().map(this::toDomainUser).toList();
    }

    @Override
    public Long countUser(String username, String role) {
        return userDao.count(username, role);
    }

    @Override
    public User queryUserById(Long id) {
        return toDomainUser(userDao.queryById(id));
    }

    @Override
    public User queryUserByUsername(String username) {
        return toDomainUser(userDao.queryByUsername(username));
    }

    @Override
    public void insertUser(UserManageRequest request) {
        com.dasi.infrastructure.persistent.po.User po = toUserPo(request);
        userDao.insert(po);
    }

    @Override
    public void updateUser(UserManageRequest request) {
        userDao.update(toUserPo(request));
    }

    @Override
    public void deleteUser(Long id) {
        userDao.delete(id);
    }

    // -------------------- convert helpers --------------------

    private AiApi toPo(AdminApi api) {
        if (api == null) {
            return null;
        }
        return AiApi.builder()
                .id(api.getId())
                .apiId(api.getApiId())
                .apiBaseUrl(api.getApiBaseUrl())
                .apiKey(api.getApiKey())
                .apiCompletionsPath(api.getApiCompletionsPath())
                .apiEmbeddingsPath(api.getApiEmbeddingsPath())
                .apiStatus(api.getApiStatus())
                .createTime(api.getCreateTime())
                .updateTime(api.getUpdateTime())
                .build();
    }

    private AdminModel toAdminModel(AiModel po) {
        if (po == null) {
            return null;
        }
        return AdminModel.builder()
                .id(po.getId())
                .modelId(po.getModelId())
                .apiId(po.getApiId())
                .modelName(po.getModelName())
                .modelType(po.getModelType())
                .modelStatus(po.getModelStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiModel toModelPo(ModelManageRequest request) {
        if (request == null) {
            return null;
        }
        return AiModel.builder()
                .id(request.getId())
                .modelId(request.getModelId())
                .apiId(request.getApiId())
                .modelName(request.getModelName())
                .modelType(request.getModelType())
                .modelStatus(request.getModelStatus())
                .build();
    }

    private AdminMcp toAdminMcp(AiMcp po) {
        if (po == null) {
            return null;
        }
        return AdminMcp.builder()
                .id(po.getId())
                .mcpId(po.getMcpId())
                .mcpName(po.getMcpName())
                .mcpType(po.getMcpType())
                .mcpConfig(po.getMcpConfig())
                .mcpDesc(po.getMcpDesc())
                .mcpTimeout(po.getMcpTimeout())
                .mcpChat(po.getMcpChat())
                .mcpStatus(po.getMcpStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiMcp toMcpPo(McpManageRequest request) {
        if (request == null) {
            return null;
        }
        return AiMcp.builder()
                .id(request.getId())
                .mcpId(request.getMcpId())
                .mcpName(request.getMcpName())
                .mcpType(request.getMcpType())
                .mcpConfig(request.getMcpConfig())
                .mcpDesc(request.getMcpDesc())
                .mcpTimeout(request.getMcpTimeout())
                .mcpChat(request.getMcpChat())
                .mcpStatus(request.getMcpStatus())
                .build();
    }

    private AdminAdvisor toAdminAdvisor(AiAdvisor po) {
        if (po == null) {
            return null;
        }
        return AdminAdvisor.builder()
                .id(po.getId())
                .advisorId(po.getAdvisorId())
                .advisorName(po.getAdvisorName())
                .advisorType(po.getAdvisorType())
                .advisorDesc(po.getAdvisorDesc())
                .advisorOrder(po.getAdvisorOrder())
                .advisorParam(po.getAdvisorParam())
                .advisorStatus(po.getAdvisorStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiAdvisor toAdvisorPo(AdvisorManageRequest request) {
        if (request == null) {
            return null;
        }
        return AiAdvisor.builder()
                .id(request.getId())
                .advisorId(request.getAdvisorId())
                .advisorName(request.getAdvisorName())
                .advisorType(request.getAdvisorType())
                .advisorDesc(request.getAdvisorDesc())
                .advisorOrder(request.getAdvisorOrder())
                .advisorParam(request.getAdvisorParam())
                .advisorStatus(request.getAdvisorStatus())
                .build();
    }

    private AdminPrompt toAdminPrompt(AiPrompt po) {
        if (po == null) {
            return null;
        }
        return AdminPrompt.builder()
                .id(po.getId())
                .promptId(po.getPromptId())
                .promptName(po.getPromptName())
                .promptContent(po.getPromptContent())
                .promptDesc(po.getPromptDesc())
                .promptStatus(po.getPromptStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiPrompt toPromptPo(PromptManageRequest request) {
        if (request == null) {
            return null;
        }
        return AiPrompt.builder()
                .id(request.getId())
                .promptId(request.getPromptId())
                .promptName(request.getPromptName())
                .promptContent(request.getPromptContent())
                .promptDesc(request.getPromptDesc())
                .promptStatus(request.getPromptStatus())
                .build();
    }

    private AdminClient toAdminClient(AiClient po) {
        if (po == null) {
            return null;
        }
        return AdminClient.builder()
                .id(po.getId())
                .clientId(po.getClientId())
                .clientType(po.getClientType())
                .modelId(po.getModelId())
                .modelName(po.getModelName())
                .clientName(po.getClientName())
                .clientDesc(po.getClientDesc())
                .clientStatus(po.getClientStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClient toClientPo(ClientManageRequest request) {
        if (request == null) {
            return null;
        }
        return AiClient.builder()
                .id(request.getId())
                .clientId(request.getClientId())
                .clientType(request.getClientType())
                .modelId(request.getModelId())
                .modelName(request.getModelName())
                .clientName(request.getClientName())
                .clientDesc(request.getClientDesc())
                .clientStatus(request.getClientStatus())
                .build();
    }

    private AdminFlow toAdminFlow(AiFlow po) {
        if (po == null) {
            return null;
        }
        return AdminFlow.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .clientId(po.getClientId())
                .clientType(po.getClientType())
                .flowPrompt(po.getFlowPrompt())
                .flowSeq(po.getFlowSeq())
                .flowStatus(po.getFlowStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiFlow toFlowPo(FlowManageRequest request) {
        if (request == null) {
            return null;
        }
        return AiFlow.builder()
                .id(request.getId())
                .agentId(request.getAgentId())
                .clientId(request.getClientId())
                .clientType(request.getClientType())
                .flowPrompt(request.getFlowPrompt())
                .flowSeq(request.getFlowSeq())
                .flowStatus(request.getFlowStatus())
                .build();
    }

    private AdminAgent toAdminAgent(AiAgent po) {
        if (po == null) {
            return null;
        }
        return AdminAgent.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .agentName(po.getAgentName())
                .agentType(po.getAgentType())
                .agentDesc(po.getAgentDesc())
                .agentStatus(po.getAgentStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiAgent toAgentPo(AgentManageRequest request) {
        if (request == null) {
            return null;
        }
        return AiAgent.builder()
                .id(request.getId())
                .agentId(request.getAgentId())
                .agentName(request.getAgentName())
                .agentType(request.getAgentType())
                .agentDesc(request.getAgentDesc())
                .agentStatus(request.getAgentStatus())
                .build();
    }

    private User toDomainUser(com.dasi.infrastructure.persistent.po.User po) {
        if (po == null) {
            return null;
        }
        return User.builder()
                .id(po.getId())
                .username(po.getUsername())
                .password(po.getPassword())
                .role(po.getRole())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private com.dasi.infrastructure.persistent.po.User toUserPo(UserManageRequest request) {
        if (request == null) {
            return null;
        }
        com.dasi.infrastructure.persistent.po.User po = new com.dasi.infrastructure.persistent.po.User();
        po.setId(request.getId());
        po.setUsername(request.getUsername());
        po.setPassword(request.getPassword());
        po.setRole(request.getRole());
        return po;
    }
}
