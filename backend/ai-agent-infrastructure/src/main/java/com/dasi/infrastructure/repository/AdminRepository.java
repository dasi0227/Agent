package com.dasi.infrastructure.repository;

import com.dasi.domain.admin.model.enumeration.AiConfigType;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.types.annotation.CacheEvict;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.query.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.domain.admin.model.enumeration.AiConfigType.*;

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
    private IAiConfigDao aiConfigDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IUserDao userDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiTaskDao aiTaskDao;

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
    public ApiVO apiQuery(Long id) {
        AiApi aiApi = aiApiDao.queryById(id);
        return toApiVO(aiApi);
    }

    @Override
    public ApiVO apiQuery(String apiId) {
        return toApiVO(aiApiDao.queryByApiId(apiId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void apiInsert(ApiManageRequest apiManageRequest) {
        AiApi aiApi = toApiPO(apiManageRequest);
        aiApiDao.insert(aiApi);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void apiUpdate(ApiManageRequest apiManageRequest) {
        AiApi aiApi = toApiPO(apiManageRequest);
        aiApiDao.update(aiApi);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void apiDelete(Long id) {
        aiApiDao.delete(id);
    }

    // -------------------- Model --------------------
    @Override
    public List<ModelVO> modelPage(ModelPageRequest request) {

        String idKeyword = request.getIdKeyword();
        String nameKeyword = request.getNameKeyword();
        String apiId = request.getApiId();
        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        Integer offset = (pageNum - 1) * pageSize;

        List<AiModel> poList = aiModelDao.page(idKeyword, nameKeyword, apiId, offset, pageSize);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }

        return poList.stream().map(this::toModelVO).toList();
    }

    @Override
    public Integer modelCount(ModelPageRequest request) {
        return aiModelDao.count(request.getIdKeyword(), request.getNameKeyword());
    }

    @Override
    public ModelVO modelQuery(Long id) {
        AiModel po = aiModelDao.queryById(id);
        return toModelVO(po);
    }

    @Override
    public ModelVO modelQuery(String modelId) {
        return toModelVO(aiModelDao.queryByModelId(modelId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void modelInsert(ModelManageRequest request) {
        aiModelDao.insert(toModelPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void modelUpdate(ModelManageRequest request) {
        aiModelDao.update(toModelPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void modelDelete(Long id) {
        aiModelDao.delete(id);
    }

    // -------------------- MCP --------------------
    @Override
    public List<McpVO> mcpPage(McpPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiMcp> poList = aiMcpDao.page(request.getIdKeyword(), request.getNameKeyword(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toMcpVO).toList();
    }

    @Override
    public Integer mcpCount(McpPageRequest request) {
        return aiMcpDao.count(request.getIdKeyword(), request.getNameKeyword());
    }

    @Override
    public McpVO mcpQuery(Long id) {
        return toMcpVO(aiMcpDao.queryById(id));
    }

    @Override
    public McpVO mcpQuery(String mcpId) {
        return toMcpVO(aiMcpDao.queryByMcpId(mcpId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void mcpInsert(McpManageRequest request) {
        aiMcpDao.insert(toMcpPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void mcpUpdate(McpManageRequest request) {
        aiMcpDao.update(toMcpPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void mcpDelete(Long id) {
        aiMcpDao.delete(id);
    }

    // -------------------- Advisor --------------------
    @Override
    public List<AdvisorVO> advisorPage(AdvisorPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiAdvisor> poList = aiAdvisorDao.page(request.getIdKeyword(), request.getNameKeyword(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdvisorVO).toList();
    }

    @Override
    public Integer advisorCount(AdvisorPageRequest request) {
        return aiAdvisorDao.count(request.getIdKeyword(), request.getNameKeyword());
    }

    @Override
    public AdvisorVO advisorQuery(Long id) {
        return toAdvisorVO(aiAdvisorDao.queryById(id));
    }

    @Override
    public AdvisorVO advisorQuery(String advisorId) {
        return toAdvisorVO(aiAdvisorDao.queryByAdvisorId(advisorId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void advisorInsert(AdvisorManageRequest request) {
        aiAdvisorDao.insert(toAdvisorPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void advisorUpdate(AdvisorManageRequest request) {
        aiAdvisorDao.update(toAdvisorPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void advisorDelete(Long id) {
        aiAdvisorDao.delete(id);
    }

    // -------------------- Prompt --------------------
    @Override
    public List<PromptVO> promptPage(PromptPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiPrompt> poList = aiPromptDao.page(request.getIdKeyword(), request.getNameKeyword(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toPromptVO).toList();
    }

    @Override
    public Integer promptCount(PromptPageRequest request) {
        return aiPromptDao.count(request.getIdKeyword(), request.getNameKeyword());
    }

    @Override
    public PromptVO promptQuery(Long id) {
        return toPromptVO(aiPromptDao.queryById(id));
    }

    @Override
    public PromptVO promptQuery(String promptId) {
        return toPromptVO(aiPromptDao.queryByPromptId(promptId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void promptInsert(PromptManageRequest request) {
        aiPromptDao.insert(toPromptPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void promptUpdate(PromptManageRequest request) {
        aiPromptDao.update(toPromptPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void promptDelete(Long id) {
        aiPromptDao.delete(id);
    }

    // -------------------- Client --------------------
    @Override
    public List<ClientVO> clientPage(ClientPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiClient> poList = aiClientDao.page(request.getIdKeyword(), request.getNameKeyword(), request.getModelId(), request.getClientType(), request.getClientRole(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toClientVO).toList();
    }

    @Override
    public Integer clientCount(ClientPageRequest request) {
        return aiClientDao.count(request.getIdKeyword(), request.getNameKeyword(), request.getModelId(), request.getClientType(), request.getClientRole());
    }

    @Override
    public ClientVO clientQuery(Long id) {
        return toClientVO(aiClientDao.queryById(id));
    }

    @Override
    public ClientVO clientQuery(String clientId) {
        return toClientVO(aiClientDao.queryByClientId(clientId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void clientInsert(ClientManageRequest request) {
        aiClientDao.insert(toClientPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void clientUpdate(ClientManageRequest request) {
        aiClientDao.update(toClientPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void clientDelete(Long id) {
        aiClientDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void clientToggle(Long id, Integer status) {
        AiClient po = AiClient.builder()
                .id(id)
                .clientStatus(status)
                .build();
        aiClientDao.toggle(po);
    }

    // -------------------- Agent --------------------
    @Override
    public List<AgentVO> agentPage(AgentPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiAgent> poList = aiAgentDao.page(request.getIdKeyword(), request.getNameKeyword(), request.getAgentType(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    public List<AgentVO> agentList(AgentListRequest request) {
        List<AiAgent> poList = aiAgentDao.list(request.getIdKeyword(), request.getNameKeyword(), request.getAgentType());
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    public Integer agentCount(AgentPageRequest request) {
        return aiAgentDao.count(request.getIdKeyword(), request.getNameKeyword(), request.getAgentType());
    }

    @Override
    public AgentVO agentQuery(Long id) {
        return toAgentVO(aiAgentDao.queryById(id));
    }

    @Override
    public AgentVO agentQuery(String agentId) {
        return toAgentVO(aiAgentDao.queryAgentByAgentId(agentId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void agentInsert(AgentManageRequest request) {
        aiAgentDao.insert(toAgentPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void agentUpdate(AgentManageRequest request) {
        aiAgentDao.update(toAgentPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void agentDelete(Long id) {
        aiAgentDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void agentToggle(Long id, Integer status) {
        AiAgent po = AiAgent.builder()
                .id(id)
                .agentStatus(status)
                .build();
        aiAgentDao.toggle(po);
    }

    // -------------------- User --------------------
    @Override
    public List<UserVO> userPage(UserPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<User> poList = userDao.page(request.getUsernameKeyWord(), request.getRole(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toUserVO).toList();
    }

    @Override
    public Integer userCount(UserPageRequest request) {
        Long count = userDao.count(request.getUsernameKeyWord(), request.getRole());
        return count == null ? 0 : count.intValue();
    }

    @Override
    public UserVO userQuery(Long id) {
        return toUserVO(userDao.queryById(id));
    }

    @Override
    public UserVO userQuery(String username) {
        return toUserVO(userDao.queryByUsername(username));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void userInsert(UserManageRequest request) {
        userDao.insert(toUserPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void userUpdate(UserManageRequest request) {
        userDao.update(toUserPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void userDelete(Long id) {
        userDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void userToggle(Long id, Integer status) {
        userDao.toggle(id, status);
    }

    // -------------------- Config --------------------
    @Override
    public List<ConfigVO> configList(ConfigListRequest request) {
        List<AiConfig> poList = aiConfigDao.list(request.getIdKeyword(), request.getValueKeyword(), request.getConfigType());
        return poList.stream().map(this::toConfigVO).toList();
    }

    @Override
    public ConfigVO configQuery(ConfigManageRequest request) {
        return toConfigVO(aiConfigDao.queryByUniqueKey(request.getClientId(), request.getConfigType(), request.getConfigValue()));
    }

    @Override
    public ConfigVO configQuery(Long id) {
        return toConfigVO(aiConfigDao.queryById(id));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void configInsert(ConfigManageRequest request) {
        aiConfigDao.insert(toConfigPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void configUpdate(ConfigManageRequest request) {
        aiConfigDao.update(toConfigPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void configDelete(Long id) {
        aiConfigDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void configToggle(Long id, Integer status) {
        aiConfigDao.toggle(id, status);
    }

    // -------------------- Flow --------------------
    @Override
    public List<ClientDetailVO> flowClient() {
        List<ClientDetailVO> clientDetailVOList = new ArrayList<>();

        List<AiClient> aiClientList = aiClientDao.queryWorkClientList();
        if (aiClientList.isEmpty()) {
            return clientDetailVOList;
        }

        for (AiClient aiClient : aiClientList) {
            String clientId = aiClient.getClientId();
            String clientRole = aiClient.getClientRole();
            ClientVO clientVO = toClientVO(aiClient);

            String modelId = aiClient.getModelId();
            AiModel aiModel = aiModelDao.queryByModelId(modelId);
            ModelVO modelVO = toModelVO(aiModel);

            String apiId = aiModel.getApiId();
            AiApi aiApi = aiApiDao.queryByApiId(apiId);
            ApiVO apiVO = toApiVO(aiApi);

            List<AiConfig> aiConfigList = aiConfigDao.queryByClientId(clientId);
            if (aiConfigList.isEmpty()) {
                continue;
            }

            List<McpVO> mcpVOList = new ArrayList<>();
            List<AdvisorVO> advisorVOList = new ArrayList<>();
            List<PromptVO> promptVOList = new ArrayList<>();
            for (AiConfig aiConfig : aiConfigList) {
                switch (AiConfigType.fromString(aiConfig.getConfigType())) {
                    case MCP -> {
                        AiMcp aiMcp = aiMcpDao.queryByMcpId(aiConfig.getConfigValue());
                        mcpVOList.add(toMcpVO(aiMcp));
                    }
                    case ADVISOR -> {
                        AiAdvisor aiAdvisor = aiAdvisorDao.queryByAdvisorId(aiConfig.getConfigValue());
                        advisorVOList.add(toAdvisorVO(aiAdvisor));
                    }
                    case PROMPT -> {
                        AiPrompt aiPrompt = aiPromptDao.queryByPromptId(aiConfig.getConfigValue());
                        promptVOList.add(toPromptVO(aiPrompt));
                    }
                }
            }

            ClientDetailVO clientDetailVO = ClientDetailVO.builder()
                    .clientId(clientId)
                    .clientRole(clientRole)
                    .client(clientVO)
                    .model(modelVO)
                    .api(apiVO)
                    .mcpList(mcpVOList)
                    .advisorList(advisorVOList)
                    .promptList(promptVOList)
                    .build();

            clientDetailVOList.add(clientDetailVO);
        }

        return clientDetailVOList;
    }

    @Override
    public List<FlowVO> flowAgent(String agentId) {
        List<AiFlow> poList = aiFlowDao.queryByAgentId(agentId);
        return poList.stream().map(this::toFlowVO).toList();
    }

    @Override
    public FlowVO flowQuery(String agentId, String clientId) {
        return toFlowVO(aiFlowDao.queryByAgentIdAndClientId(agentId, clientId));
    }

    @Override
    public FlowVO flowQuery(Long id) {
        return toFlowVO(aiFlowDao.queryById(id));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void flowInsert(FlowManageRequest request) {
        aiFlowDao.insert(toFlowPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void flowUpdate(FlowManageRequest request) {
        aiFlowDao.update(toFlowPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void flowDelete(Long id) {
        aiFlowDao.delete(id);
    }

    // -------------------- Task --------------------
    @Override
    public List<TaskVO> taskPage(TaskPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiTask> poList = aiTaskDao.page(request.getIdKeyword(), request.getAgentId(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toTaskVO).toList();
    }

    @Override
    public Integer taskCount(TaskPageRequest request) {
        return aiTaskDao.count(request.getIdKeyword(), request.getAgentId());
    }

    @Override
    public TaskVO taskQuery(Long id) {
        return toTaskVO(aiTaskDao.queryById(id));
    }

    @Override
    public TaskVO taskQuery(String taskId) {
        return toTaskVO(aiTaskDao.queryByTaskId(taskId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void taskInsert(TaskManageRequest request) {
        aiTaskDao.insert(toTaskPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void taskUpdate(TaskManageRequest request) {
        aiTaskDao.update(toTaskPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void taskDelete(Long id) {
        aiTaskDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:"})
    public void taskToggle(Long id, Integer status) {
        AiTask po = AiTask.builder()
                .id(id)
                .taskStatus(status)
                .build();
        aiTaskDao.toggle(po);
    }

    // -------------------- Depend --------------------
    @Override
    public List<String> queryClientDependOnPrompt(String promptId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(PROMPT.getType(), promptId);
    }

    @Override
    public List<String> queryClientDependOnAdvisor(String advisorId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(ADVISOR.getType(), advisorId);
    }

    @Override
    public List<String> queryClientDependOnMcp(String mcpId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(MCP.getType(), mcpId);
    }

    @Override
    public List<String> queryModelDependOnApi(String apiId) {
        return aiModelDao.queryModelIdByApiId(apiId);
    }

    @Override
    public List<String> queryClientDependOnModel(String modelId) {
        return aiClientDao.queryClientIdByModelId(modelId);
    }

    @Override
    public List<String> queryAgentDependOnClient(String clientId) {
        return aiFlowDao.queryAgentIdByClientId(clientId);
    }

    @Override
    public List<String> listApiId() {
        return aiApiDao.listApiId();
    }

    @Override
    public List<String> listModelId() {
        return aiModelDao.listModelId();
    }

    // -------------------- Util --------------------
    private ApiVO toApiVO(AiApi po) {
        if (po == null) {
            return null;
        }
        return ApiVO.builder()
                .id(po.getId())
                .apiId(po.getApiId())
                .apiBaseUrl(po.getApiBaseUrl())
                .apiKey(po.getApiKey())
                .apiCompletionsPath(po.getApiCompletionsPath())
                .apiEmbeddingsPath(po.getApiEmbeddingsPath())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiApi toApiPO(ApiManageRequest request) {
        return AiApi.builder()
                .id(request.getId())
                .apiId(request.getApiId())
                .apiBaseUrl(request.getApiBaseUrl())
                .apiKey(request.getApiKey())
                .apiCompletionsPath(request.getApiCompletionsPath())
                .apiEmbeddingsPath(request.getApiEmbeddingsPath())
                .build();
    }

    private AiModel toModelPo(ModelManageRequest request) {
        return AiModel.builder()
                .id(request.getId())
                .modelId(request.getModelId())
                .apiId(request.getApiId())
                .modelName(request.getModelName())
                .modelType(request.getModelType())
                .build();
    }

    private ModelVO toModelVO(AiModel po) {
        if (po == null) {
            return null;
        }
        return ModelVO.builder()
                .id(po.getId())
                .modelId(po.getModelId())
                .apiId(po.getApiId())
                .modelName(po.getModelName())
                .modelType(po.getModelType())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private McpVO toMcpVO(AiMcp po) {
        if (po == null) {
            return null;
        }
        return McpVO.builder()
                .id(po.getId())
                .mcpId(po.getMcpId())
                .mcpName(po.getMcpName())
                .mcpType(po.getMcpType())
                .mcpConfig(po.getMcpConfig())
                .mcpDesc(po.getMcpDesc())
                .mcpTimeout(po.getMcpTimeout())
                .mcpChat(po.getMcpChat())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiMcp toMcpPo(McpManageRequest request) {
        return AiMcp.builder()
                .id(request.getId())
                .mcpId(request.getMcpId())
                .mcpName(request.getMcpName())
                .mcpType(request.getMcpType())
                .mcpConfig(request.getMcpConfig())
                .mcpDesc(request.getMcpDesc())
                .mcpTimeout(request.getMcpTimeout())
                .mcpChat(request.getMcpChat())
                .build();
    }

    private AdvisorVO toAdvisorVO(AiAdvisor po) {
        if (po == null) {
            return null;
        }
        return AdvisorVO.builder()
                .id(po.getId())
                .advisorId(po.getAdvisorId())
                .advisorName(po.getAdvisorName())
                .advisorType(po.getAdvisorType())
                .advisorDesc(po.getAdvisorDesc())
                .advisorOrder(po.getAdvisorOrder())
                .advisorParam(po.getAdvisorParam())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiAdvisor toAdvisorPo(AdvisorManageRequest request) {
        return AiAdvisor.builder()
                .id(request.getId())
                .advisorId(request.getAdvisorId())
                .advisorName(request.getAdvisorName())
                .advisorType(request.getAdvisorType())
                .advisorDesc(request.getAdvisorDesc())
                .advisorOrder(request.getAdvisorOrder())
                .advisorParam(request.getAdvisorParam())
                .build();
    }

    private PromptVO toPromptVO(AiPrompt po) {
        if (po == null) {
            return null;
        }
        return PromptVO.builder()
                .id(po.getId())
                .promptId(po.getPromptId())
                .promptName(po.getPromptName())
                .promptContent(po.getPromptContent())
                .promptDesc(po.getPromptDesc())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiPrompt toPromptPo(PromptManageRequest request) {
        return AiPrompt.builder()
                .id(request.getId())
                .promptId(request.getPromptId())
                .promptName(request.getPromptName())
                .promptContent(request.getPromptContent())
                .promptDesc(request.getPromptDesc())
                .build();
    }

    private ClientVO toClientVO(AiClient po) {
        if (po == null) {
            return null;
        }
        return ClientVO.builder()
                .id(po.getId())
                .clientId(po.getClientId())
                .clientType(po.getClientType())
                .clientRole(po.getClientRole())
                .modelId(po.getModelId())
                .modelName(po.getModelName())
                .clientName(po.getClientName())
                .clientDesc(po.getClientDesc())
                .clientStatus(po.getClientStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClient toClientPo(ClientManageRequest request) {
        return AiClient.builder()
                .id(request.getId())
                .clientId(request.getClientId())
                .clientType(request.getClientType())
                .clientRole(request.getClientRole())
                .modelId(request.getModelId())
                .modelName(request.getModelName())
                .clientName(request.getClientName())
                .clientDesc(request.getClientDesc())
                .clientStatus(request.getClientStatus())
                .build();
    }

    private AgentVO toAgentVO(AiAgent po) {
        if (po == null) {
            return null;
        }
        return AgentVO.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .agentName(po.getAgentName())
                .agentType(po.getAgentType())
                .agentDesc(po.getAgentDesc())
                .agentStatus(po.getAgentStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiAgent toAgentPo(AgentManageRequest request) {
        return AiAgent.builder()
                .id(request.getId())
                .agentId(request.getAgentId())
                .agentName(request.getAgentName())
                .agentType(request.getAgentType())
                .agentDesc(request.getAgentDesc())
                .agentStatus(request.getAgentStatus())
                .build();
    }

    private UserVO toUserVO(User po) {
        if (po == null) {
            return null;
        }
        return UserVO.builder()
                .id(po.getId())
                .username(po.getUsername())
                .role(po.getRole())
                .userStatus(po.getUserStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private User toUserPo(UserManageRequest request) {
        return User.builder()
                .id(request.getId())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole())
                .userStatus(request.getUserStatus())
                .build();
    }

    private AiConfig toConfigPO(ConfigManageRequest request) {
        return AiConfig.builder()
                .id(request.getId())
                .clientId(request.getClientId())
                .configType(request.getConfigType())
                .configValue(request.getConfigValue())
                .configParam(request.getConfigParam())
                .configStatus(request.getConfigStatus())
                .build();
    }

    private ConfigVO toConfigVO(AiConfig po) {
        if (po == null) {
            return null;
        }
        return ConfigVO.builder()
                .id(po.getId())
                .clientId(po.getClientId())
                .configType(po.getConfigType())
                .configValue(po.getConfigValue())
                .configParam(po.getConfigParam())
                .configStatus(po.getConfigStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private FlowVO toFlowVO(AiFlow po) {
        if (po == null) {
            return null;
        }
        return FlowVO.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .clientId(po.getClientId())
                .clientRole(po.getClientRole())
                .flowPrompt(po.getFlowPrompt())
                .flowSeq(po.getFlowSeq())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiFlow toFlowPO(FlowManageRequest request) {
        return AiFlow.builder()
                .id(request.getId())
                .agentId(request.getAgentId())
                .clientId(request.getClientId())
                .clientRole(request.getClientRole())
                .flowPrompt(request.getFlowPrompt())
                .flowSeq(request.getFlowSeq())
                .build();
    }

    private TaskVO toTaskVO(AiTask po) {
        if (po == null) {
            return null;
        }
        return TaskVO.builder()
                .id(po.getId())
                .taskId(po.getTaskId())
                .agentId(po.getAgentId())
                .taskCron(po.getTaskCron())
                .taskDesc(po.getTaskDesc())
                .taskParam(po.getTaskParam())
                .taskStatus(po.getTaskStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiTask toTaskPO(TaskManageRequest request) {
        return AiTask.builder()
                .id(request.getId())
                .taskId(request.getTaskId())
                .agentId(request.getAgentId())
                .taskCron(request.getTaskCron())
                .taskDesc(request.getTaskDesc())
                .taskParam(request.getTaskParam())
                .taskStatus(request.getTaskStatus())
                .build();
    }

}
