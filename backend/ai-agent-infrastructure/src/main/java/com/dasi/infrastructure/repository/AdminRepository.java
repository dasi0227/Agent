package com.dasi.infrastructure.repository;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.page.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

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
    public void apiDelete(Long id) {
        aiApiDao.delete(id);
    }

    @Override
    public void apiToggle(Long id, Integer apiStatus) {
        AiApi aiApi = AiApi.builder().id(id).apiStatus(apiStatus).build();
        aiApiDao.toggle(aiApi);
    }

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
                .apiStatus(po.getApiStatus())
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
                .apiStatus(request.getApiStatus())
                .build();
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
    public void modelInsert(ModelManageRequest request) {
        aiModelDao.insert(toModelPo(request));
    }

    @Override
    public void modelUpdate(ModelManageRequest request) {
        aiModelDao.update(toModelPo(request));
    }

    @Override
    public void modelDelete(Long id) {
        aiModelDao.delete(id);
    }

    @Override
    public void modelToggle(Long id, Integer status) {
        AiModel po = AiModel.builder()
                .id(id)
                .modelStatus(status)
                .build();
        aiModelDao.toggle(po);
    }

    private AiModel toModelPo(ModelManageRequest request) {
        return AiModel.builder()
                .id(request.getId())
                .modelId(request.getModelId())
                .apiId(request.getApiId())
                .modelName(request.getModelName())
                .modelType(request.getModelType())
                .modelStatus(request.getModelStatus())
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
                .modelStatus(po.getModelStatus())
                .updateTime(po.getUpdateTime())
                .build();
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
    public void mcpInsert(McpManageRequest request) {
        aiMcpDao.insert(toMcpPo(request));
    }

    @Override
    public void mcpUpdate(McpManageRequest request) {
        aiMcpDao.update(toMcpPo(request));
    }

    @Override
    public void mcpDelete(Long id) {
        aiMcpDao.delete(id);
    }

    @Override
    public void mcpToggle(Long id, Integer status) {
        AiMcp po = AiMcp.builder()
                .id(id)
                .mcpStatus(status)
                .build();
        aiMcpDao.toggle(po);
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
                .mcpStatus(po.getMcpStatus())
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
                .mcpStatus(request.getMcpStatus())
                .build();
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
    public void advisorInsert(AdvisorManageRequest request) {
        aiAdvisorDao.insert(toAdvisorPo(request));
    }

    @Override
    public void advisorUpdate(AdvisorManageRequest request) {
        aiAdvisorDao.update(toAdvisorPo(request));
    }

    @Override
    public void advisorDelete(Long id) {
        aiAdvisorDao.delete(id);
    }

    @Override
    public void advisorToggle(Long id, Integer status) {
        AiAdvisor po = AiAdvisor.builder()
                .id(id)
                .advisorStatus(status)
                .build();
        aiAdvisorDao.toggle(po);
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
                .advisorStatus(po.getAdvisorStatus())
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
                .advisorStatus(request.getAdvisorStatus())
                .build();
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
    public void promptInsert(PromptManageRequest request) {
        aiPromptDao.insert(toPromptPo(request));
    }

    @Override
    public void promptUpdate(PromptManageRequest request) {
        aiPromptDao.update(toPromptPo(request));
    }

    @Override
    public void promptDelete(Long id) {
        aiPromptDao.delete(id);
    }

    @Override
    public void promptToggle(Long id, Integer status) {
        AiPrompt po = AiPrompt.builder()
                .id(id)
                .promptStatus(status)
                .build();
        aiPromptDao.toggle(po);
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
                .promptStatus(po.getPromptStatus())
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
                .promptStatus(request.getPromptStatus())
                .build();
    }

    // -------------------- Client --------------------
    @Override
    public List<ClientVO> clientPage(ClientPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiClient> poList = aiClientDao.page(request.getIdKeyword(), request.getNameKeyword(), request.getModelId(), request.getClientType(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toClientVO).toList();
    }

    @Override
    public Integer clientCount(ClientPageRequest request) {
        return aiClientDao.count(request.getIdKeyword(), request.getNameKeyword(), request.getModelId(), request.getClientType());
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
    public void clientInsert(ClientManageRequest request) {
        aiClientDao.insert(toClientPo(request));
    }

    @Override
    public void clientUpdate(ClientManageRequest request) {
        aiClientDao.update(toClientPo(request));
    }

    @Override
    public void clientDelete(Long id) {
        aiClientDao.delete(id);
    }

    @Override
    public void clientToggle(Long id, Integer status) {
        AiClient po = AiClient.builder()
                .id(id)
                .clientStatus(status)
                .build();
        aiClientDao.toggle(po);
    }


    private ClientVO toClientVO(AiClient po) {
        if (po == null) {
            return null;
        }
        return ClientVO.builder()
                .id(po.getId())
                .clientId(po.getClientId())
                .clientType(po.getClientType())
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
                .modelId(request.getModelId())
                .modelName(request.getModelName())
                .clientName(request.getClientName())
                .clientDesc(request.getClientDesc())
                .clientStatus(request.getClientStatus())
                .build();
    }

    // -------------------- Agent --------------------
    @Override
    public List<AdminAgentVO> agentPage(AgentPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiAgent> poList = aiAgentDao.page(request.getIdKeyword(), request.getNameKeyword(), request.getAgentType(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    public Integer agentCount(AgentPageRequest request) {
        return aiAgentDao.count(request.getIdKeyword(), request.getNameKeyword(), request.getAgentType());
    }

    @Override
    public AdminAgentVO agentQuery(Long id) {
        return toAgentVO(aiAgentDao.queryById(id));
    }

    @Override
    public AdminAgentVO agentQuery(String agentId) {
        return toAgentVO(aiAgentDao.queryAgentByAgentId(agentId));
    }

    @Override
    public void agentInsert(AgentManageRequest request) {
        aiAgentDao.insert(toAgentPo(request));
    }

    @Override
    public void agentUpdate(AgentManageRequest request) {
        aiAgentDao.update(toAgentPo(request));
    }

    @Override
    public void agentDelete(Long id) {
        aiAgentDao.delete(id);
    }

    @Override
    public void agentToggle(Long id, Integer status) {
        AiAgent po = AiAgent.builder()
                .id(id)
                .agentStatus(status)
                .build();
        aiAgentDao.toggle(po);
    }

    private AdminAgentVO toAgentVO(AiAgent po) {
        if (po == null) {
            return null;
        }
        return AdminAgentVO.builder()
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
    public void userInsert(UserManageRequest request) {
        userDao.insert(toUserPo(request));
    }

    @Override
    public void userUpdate(UserManageRequest request) {
        userDao.update(toUserPo(request));
    }

    @Override
    public void userDelete(Long id) {
        userDao.delete(id);
    }

    private UserVO toUserVO(User po) {
        if (po == null) {
            return null;
        }
        return UserVO.builder()
                .id(po.getId())
                .username(po.getUsername())
                .role(po.getRole())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private User toUserPo(UserManageRequest request) {
        return User.builder()
                .id(request.getId())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
    }

    // -------------------- Depend --------------------
    @Override
    public List<String> queryClientDependOnPrompt(String promptId) {
        return aiConfigDao.queryClientIdListByConfig(PROMPT.getType(), promptId);
    }

    @Override
    public List<String> queryClientDependOnAdvisor(String advisorId) {
        return aiConfigDao.queryClientIdListByConfig(ADVISOR.getType(), advisorId);
    }

    @Override
    public List<String> queryClientDependOnMcp(String mcpId) {
        return aiConfigDao.queryClientIdListByConfig(MCP.getType(), mcpId);
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

}
