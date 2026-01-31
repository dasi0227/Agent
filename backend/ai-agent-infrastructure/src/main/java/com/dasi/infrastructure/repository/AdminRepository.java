package com.dasi.infrastructure.repository;

import com.dasi.domain.admin.model.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.domain.login.model.User;
import com.dasi.domain.util.IRedisService;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.types.constant.RedisConstant.LIST_CHAT_CLIENT_KEY;
import static com.dasi.types.constant.RedisConstant.LIST_WORK_AGENT_KEY;
import static com.dasi.types.constant.RedisConstant.PO_ADVISOR_PREFIX;
import static com.dasi.types.constant.RedisConstant.PO_AGENT_PREFIX;
import static com.dasi.types.constant.RedisConstant.PO_API_PREFIX;
import static com.dasi.types.constant.RedisConstant.PO_CLIENT_PREFIX;
import static com.dasi.types.constant.RedisConstant.PO_LIST_CONFIG_PREFIX;
import static com.dasi.types.constant.RedisConstant.PO_LIST_FLOW_PREFIX;
import static com.dasi.types.constant.RedisConstant.PO_MCP_PREFIX;
import static com.dasi.types.constant.RedisConstant.PO_MODEL_PREFIX;
import static com.dasi.types.constant.RedisConstant.PO_PROMPT_PREFIX;

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

    @Resource
    private IRedisService redisService;

    // -------------------- API --------------------
    @Override
    public List<AdminApi> queryApiList(String keyword, Integer status, int offset, int size) {
        List<AiApi> poList = aiApiDao.queryPage(keyword, status, offset, size);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdminApi).toList();
    }

    @Override
    public Long countApi(String keyword, Integer status) {
        return aiApiDao.count(keyword, status);
    }

    @Override
    public AdminApi queryApiById(Long id) {
        return toAdminApi(aiApiDao.queryById(id));
    }

    @Override
    public AdminApi queryApiByApiId(String apiId) {
        return toAdminApi(aiApiDao.queryByApiId(apiId));
    }

    @Override
    public void insertApi(AdminApi api) {
        AiApi po = toPo(api);
        aiApiDao.insert(po);
        api.setId(po.getId());
        clearApiCache(api.getApiId());
    }

    @Override
    public void updateApi(AdminApi api) {
        aiApiDao.update(toPo(api));
        clearApiCache(api.getApiId());
    }

    @Override
    public void deleteApi(Long id) {
        AdminApi exist = queryApiById(id);
        aiApiDao.delete(id);
        if (exist != null) {
            clearApiCache(exist.getApiId());
        }
    }

    @Override
    public List<String> queryModelIdListByApiId(String apiId) {
        return aiModelDao.queryModelIdListByApiId(apiId);
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
    public void insertModel(AdminModel model) {
        AiModel po = toPo(model);
        aiModelDao.insert(po);
        model.setId(po.getId());
        clearModelCache(model.getModelId());
    }

    @Override
    public void updateModel(AdminModel model) {
        aiModelDao.update(toPo(model));
        clearModelCache(model.getModelId());
    }

    @Override
    public void deleteModel(Long id) {
        AdminModel exist = queryModelById(id);
        aiModelDao.delete(id);
        if (exist != null) {
            clearModelCache(exist.getModelId());
        }
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
    public void insertMcp(AdminMcp mcp) {
        AiMcp po = toPo(mcp);
        aiMcpDao.insert(po);
        mcp.setId(po.getId());
        clearMcpCache(mcp.getMcpId());
    }

    @Override
    public void updateMcp(AdminMcp mcp) {
        aiMcpDao.update(toPo(mcp));
        clearMcpCache(mcp.getMcpId());
    }

    @Override
    public void deleteMcp(Long id) {
        AdminMcp exist = queryMcpById(id);
        aiMcpDao.delete(id);
        if (exist != null) {
            clearMcpCache(exist.getMcpId());
        }
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
    public void insertAdvisor(AdminAdvisor advisor) {
        AiAdvisor po = toPo(advisor);
        aiAdvisorDao.insert(po);
        advisor.setId(po.getId());
        clearAdvisorCache(advisor.getAdvisorId());
    }

    @Override
    public void updateAdvisor(AdminAdvisor advisor) {
        aiAdvisorDao.update(toPo(advisor));
        clearAdvisorCache(advisor.getAdvisorId());
    }

    @Override
    public void deleteAdvisor(Long id) {
        AdminAdvisor exist = queryAdvisorById(id);
        aiAdvisorDao.delete(id);
        if (exist != null) {
            clearAdvisorCache(exist.getAdvisorId());
        }
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
    public void insertPrompt(AdminPrompt prompt) {
        AiPrompt po = toPo(prompt);
        aiPromptDao.insert(po);
        prompt.setId(po.getId());
        clearPromptCache(prompt.getPromptId());
    }

    @Override
    public void updatePrompt(AdminPrompt prompt) {
        aiPromptDao.update(toPo(prompt));
        clearPromptCache(prompt.getPromptId());
    }

    @Override
    public void deletePrompt(Long id) {
        AdminPrompt exist = queryPromptById(id);
        aiPromptDao.delete(id);
        if (exist != null) {
            clearPromptCache(exist.getPromptId());
        }
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
    public void insertClient(AdminClient client) {
        AiClient po = toPo(client);
        aiClientDao.insert(po);
        client.setId(po.getId());
        clearClientCache(client.getClientId());
    }

    @Override
    public void updateClient(AdminClient client) {
        aiClientDao.update(toPo(client));
        clearClientCache(client.getClientId());
    }

    @Override
    public void deleteClient(Long id) {
        AdminClient exist = queryClientById(id);
        aiClientDao.delete(id);
        if (exist != null) {
            clearClientCache(exist.getClientId());
        }
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
    public void insertFlow(AdminFlow flow) {
        AiFlow po = toPo(flow);
        aiFlowDao.insert(po);
        flow.setId(po.getId());
        clearFlowCache(flow.getAgentId());
    }

    @Override
    public void updateFlow(AdminFlow flow) {
        aiFlowDao.update(toPo(flow));
        clearFlowCache(flow.getAgentId());
    }

    @Override
    public void deleteFlow(Long id) {
        AdminFlow exist = queryFlowById(id);
        aiFlowDao.delete(id);
        if (exist != null) {
            clearFlowCache(exist.getAgentId());
        }
    }

    @Override
    public void updateFlowStatus(Long id, Integer status) {
        aiFlowDao.updateStatus(id, status);
        AdminFlow exist = queryFlowById(id);
        if (exist != null) {
            clearFlowCache(exist.getAgentId());
        }
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
    public AdminAgent queryAgentById(Long id) {
        AiAgent po = aiAgentDao.queryAgentById(id);
        return toAdminAgent(po);
    }

    @Override
    public AdminAgent queryAgentByAgentId(String agentId) {
        AiAgent po = aiAgentDao.queryAgentByAgentId(agentId);
        return toAdminAgent(po);
    }

    @Override
    public void insertAgent(AdminAgent agent) {
        AiAgent po = toPo(agent);
        aiAgentDao.insertAgent(po);
        agent.setId(po.getId());
        clearAgentCache(agent.getAgentId());
    }

    @Override
    public void updateAgent(AdminAgent agent) {
        aiAgentDao.updateAgent(toPo(agent));
        clearAgentCache(agent.getAgentId());
    }

    @Override
    public void deleteAgent(Long id) {
        AdminAgent exist = queryAgentById(id);
        aiAgentDao.deleteAgent(id);
        if (exist != null) {
            clearAgentCache(exist.getAgentId());
        } else {
            redisService.delete(LIST_WORK_AGENT_KEY);
        }
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
    public void insertUser(User user) {
        com.dasi.infrastructure.persistent.po.User po = toPo(user);
        userDao.insert(po);
        user.setId(po.getId());
    }

    @Override
    public void updateUser(User user) {
        userDao.update(toPo(user));
    }

    @Override
    public void deleteUser(Long id) {
        userDao.delete(id);
    }

    // -------------------- convert helpers --------------------
    private AdminApi toAdminApi(AiApi po) {
        if (po == null) {
            return null;
        }
        return AdminApi.builder()
                .id(po.getId())
                .apiId(po.getApiId())
                .apiBaseUrl(po.getApiBaseUrl())
                .apiKey(po.getApiKey())
                .apiCompletionsPath(po.getApiCompletionsPath())
                .apiEmbeddingsPath(po.getApiEmbeddingsPath())
                .apiStatus(po.getApiStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

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

    private AiModel toPo(AdminModel model) {
        if (model == null) {
            return null;
        }
        return AiModel.builder()
                .id(model.getId())
                .modelId(model.getModelId())
                .apiId(model.getApiId())
                .modelName(model.getModelName())
                .modelType(model.getModelType())
                .modelStatus(model.getModelStatus())
                .createTime(model.getCreateTime())
                .updateTime(model.getUpdateTime())
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

    private AiMcp toPo(AdminMcp mcp) {
        if (mcp == null) {
            return null;
        }
        return AiMcp.builder()
                .id(mcp.getId())
                .mcpId(mcp.getMcpId())
                .mcpName(mcp.getMcpName())
                .mcpType(mcp.getMcpType())
                .mcpConfig(mcp.getMcpConfig())
                .mcpDesc(mcp.getMcpDesc())
                .mcpTimeout(mcp.getMcpTimeout())
                .mcpChat(mcp.getMcpChat())
                .mcpStatus(mcp.getMcpStatus())
                .createTime(mcp.getCreateTime())
                .updateTime(mcp.getUpdateTime())
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

    private AiAdvisor toPo(AdminAdvisor advisor) {
        if (advisor == null) {
            return null;
        }
        return AiAdvisor.builder()
                .id(advisor.getId())
                .advisorId(advisor.getAdvisorId())
                .advisorName(advisor.getAdvisorName())
                .advisorType(advisor.getAdvisorType())
                .advisorDesc(advisor.getAdvisorDesc())
                .advisorOrder(advisor.getAdvisorOrder())
                .advisorParam(advisor.getAdvisorParam())
                .advisorStatus(advisor.getAdvisorStatus())
                .createTime(advisor.getCreateTime())
                .updateTime(advisor.getUpdateTime())
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

    private AiPrompt toPo(AdminPrompt prompt) {
        if (prompt == null) {
            return null;
        }
        return AiPrompt.builder()
                .id(prompt.getId())
                .promptId(prompt.getPromptId())
                .promptName(prompt.getPromptName())
                .promptContent(prompt.getPromptContent())
                .promptDesc(prompt.getPromptDesc())
                .promptStatus(prompt.getPromptStatus())
                .createTime(prompt.getCreateTime())
                .updateTime(prompt.getUpdateTime())
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

    private AiClient toPo(AdminClient client) {
        if (client == null) {
            return null;
        }
        return AiClient.builder()
                .id(client.getId())
                .clientId(client.getClientId())
                .clientType(client.getClientType())
                .modelId(client.getModelId())
                .modelName(client.getModelName())
                .clientName(client.getClientName())
                .clientDesc(client.getClientDesc())
                .clientStatus(client.getClientStatus())
                .createTime(client.getCreateTime())
                .updateTime(client.getUpdateTime())
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

    private AiFlow toPo(AdminFlow flow) {
        if (flow == null) {
            return null;
        }
        return AiFlow.builder()
                .id(flow.getId())
                .agentId(flow.getAgentId())
                .clientId(flow.getClientId())
                .clientType(flow.getClientType())
                .flowPrompt(flow.getFlowPrompt())
                .flowSeq(flow.getFlowSeq())
                .flowStatus(flow.getFlowStatus())
                .createTime(flow.getCreateTime())
                .updateTime(flow.getUpdateTime())
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

    private AiAgent toPo(AdminAgent agent) {
        if (agent == null) {
            return null;
        }
        return AiAgent.builder()
                .id(agent.getId())
                .agentId(agent.getAgentId())
                .agentName(agent.getAgentName())
                .agentType(agent.getAgentType())
                .agentDesc(agent.getAgentDesc())
                .agentStatus(agent.getAgentStatus())
                .createTime(agent.getCreateTime())
                .updateTime(agent.getUpdateTime())
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

    private com.dasi.infrastructure.persistent.po.User toPo(User user) {
        if (user == null) {
            return null;
        }
        com.dasi.infrastructure.persistent.po.User po = new com.dasi.infrastructure.persistent.po.User();
        po.setId(user.getId());
        po.setUsername(user.getUsername());
        po.setPassword(user.getPassword());
        po.setRole(user.getRole());
        po.setCreateTime(user.getCreateTime());
        po.setUpdateTime(user.getUpdateTime());
        return po;
    }

    private void clearAgentCache(String agentId) {
        redisService.delete(LIST_WORK_AGENT_KEY);
        if (agentId != null) {
            redisService.delete(PO_AGENT_PREFIX + agentId);
        }
    }

    private void clearApiCache(String apiId) {
        if (apiId == null) return;
        redisService.delete(PO_API_PREFIX + apiId);
    }

    private void clearModelCache(String modelId) {
        if (modelId == null) return;
        redisService.delete(PO_MODEL_PREFIX + modelId);
    }

    private void clearMcpCache(String mcpId) {
        if (mcpId == null) return;
        redisService.delete(PO_MCP_PREFIX + mcpId);
    }

    private void clearAdvisorCache(String advisorId) {
        if (advisorId == null) return;
        redisService.delete(PO_ADVISOR_PREFIX + advisorId);
    }

    private void clearPromptCache(String promptId) {
        if (promptId == null) return;
        redisService.delete(PO_PROMPT_PREFIX + promptId);
    }

    private void clearClientCache(String clientId) {
        if (clientId == null) return;
        redisService.delete(PO_CLIENT_PREFIX + clientId);
        redisService.delete(LIST_CHAT_CLIENT_KEY);
        redisService.delete(PO_LIST_CONFIG_PREFIX + clientId + ":advisor");
        redisService.delete(PO_LIST_CONFIG_PREFIX + clientId + ":prompt");
        redisService.delete(PO_LIST_CONFIG_PREFIX + clientId + ":mcp");
    }

    private void clearFlowCache(String agentId) {
        if (agentId == null) return;
        redisService.delete(PO_LIST_FLOW_PREFIX + agentId);
    }
}
