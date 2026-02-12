package com.dasi.infrastructure.repository;

import com.dasi.domain.admin.model.enumeration.AiConfigType;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.vo.MessageDailyCount;
import com.dasi.types.annotation.CacheEvict;
import com.dasi.types.annotation.Cacheable;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.query.*;
import com.dasi.types.dto.response.admin.DashboardResponse;
import com.dasi.types.enumeration.CacheType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dasi.domain.admin.model.enumeration.AiConfigType.*;
import static com.dasi.types.constant.RedisConstant.*;
import static com.dasi.types.constant.StatConstant.*;

@Repository
public class AdminRepository implements IAdminRepository {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    @Resource
    private ISessionDao sessionDao;

    @Resource
    private IMessageDao messageDao;

    @Resource
    private IAiStatDao aiStatDao;

    // -------------------- Dashboard --------------------
    @Override
    public DashboardResponse.CountInfo dashboardCount() {
        return DashboardResponse.CountInfo.builder()
                .apiCount(safeInt(aiApiDao.countAll()))
                .modelCount(safeInt(aiModelDao.countAll()))
                .clientCount(safeInt(aiClientDao.countAll()))
                .agentCount(safeInt(aiAgentDao.countAll()))
                .promptCount(safeInt(aiPromptDao.countAll()))
                .advisorCount(safeInt(aiAdvisorDao.countAll()))
                .mcpCount(safeInt(aiMcpDao.countAll()))
                .configCount(safeInt(aiConfigDao.countAll()))
                .flowCount(safeInt(aiFlowDao.countAll()))
                .userCount(safeInt(userDao.countAll()))
                .sessionCount(safeInt(sessionDao.countAll()))
                .messageCount(safeInt(messageDao.countAll()))
                .taskCount(safeInt(aiTaskDao.countAll()))
                .build();
    }

    private Integer safeInt(Number value) {
        return value == null ? 0 : value.intValue();
    }

    @Override
    public DashboardResponse.GraphInfo dashboardChart() {
        List<DashboardResponse.ChartValue> messageLastWeek = buildMessageChart(7);
        List<DashboardResponse.ChartValue> messageLastMonth = buildMessageChart(30);

        Map<String, List<DashboardResponse.BarValue>> workUsage = buildUsageMap(STAT_WORK);
        Map<String, List<DashboardResponse.BarValue>> chatUsage = buildUsageMap(STAT_CHAT);

        DashboardResponse.PieValue sessionWorkVsChat = DashboardResponse.PieValue.builder()
                .workCount(safeInt(sessionDao.countByType(STAT_WORK)))
                .chatCount(safeInt(sessionDao.countByType(STAT_CHAT)))
                .build();

        return DashboardResponse.GraphInfo.builder()
                .messageLastWeek(messageLastWeek)
                .messageLastMonth(messageLastMonth)
                .workUsage(workUsage)
                .chatUsage(chatUsage)
                .sessionWorkVsChat(sessionWorkVsChat)
                .build();
    }

    private Map<String, List<DashboardResponse.BarValue>> buildUsageMap(String statCategory) {
        Map<String, List<DashboardResponse.BarValue>> result = new LinkedHashMap<>();

        for (String key : STAT_USAGE_LIST) {
            List<DashboardResponse.BarValue> list = aiStatDao.sumByCategoryAndKey(statCategory, key, STAT_TOP_N)
                    .stream()
                    .map(it -> DashboardResponse.BarValue.builder()
                            .id(it.getStatValue())
                            .value(safeInt(it.getTotalCount()))
                            .build())
                    .toList();
            result.put(key, list);
        }

        return result;
    }

    private List<DashboardResponse.ChartValue> buildMessageChart(int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        Map<String, Integer> countMap = messageDao.countByDateRange(start, end)
                .stream()
                .collect(Collectors.toMap(
                        MessageDailyCount::getDay,
                        item -> safeInt(item.getCount())
                ));

        return startDate.datesUntil(today.plusDays(1))
                .map(d -> {
                    String key = d.format(dateTimeFormatter);
                    return DashboardResponse.ChartValue.builder()
                            .date(key)
                            .count(countMap.getOrDefault(key, 0))
                            .build();
                })
                .toList();
    }


    // -------------------- API --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_API_PREFIX, cacheClass = ApiVO.class, cacheType = CacheType.LIST)
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
    @Cacheable(cachePrefix = ADMIN_API_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer apiCount(ApiPageRequest apiPageRequest) {
        return aiApiDao.count(apiPageRequest.getIdKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_API_PREFIX, cacheClass = ApiVO.class, cacheType = CacheType.VALUE)
    public ApiVO apiQuery(Long id) {
        AiApi aiApi = aiApiDao.queryById(id);
        return toApiVO(aiApi);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_API_PREFIX, cacheClass = ApiVO.class, cacheType = CacheType.VALUE)
    public ApiVO apiQuery(String apiId) {
        return toApiVO(aiApiDao.queryByApiId(apiId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void apiInsert(ApiManageRequest apiManageRequest) {
        AiApi aiApi = toApiPO(apiManageRequest);
        aiApiDao.insert(aiApi);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void apiUpdate(ApiManageRequest apiManageRequest) {
        AiApi aiApi = toApiPO(apiManageRequest);
        aiApiDao.update(aiApi);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void apiDelete(Long id) {
        aiApiDao.delete(id);
    }

    // -------------------- Model --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = ModelVO.class, cacheType = CacheType.LIST)
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
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer modelCount(ModelPageRequest request) {
        return aiModelDao.count(request.getIdKeyword(), request.getNameKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = ModelVO.class, cacheType = CacheType.VALUE)
    public ModelVO modelQuery(Long id) {
        AiModel po = aiModelDao.queryById(id);
        return toModelVO(po);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = ModelVO.class, cacheType = CacheType.VALUE)
    public ModelVO modelQuery(String modelId) {
        return toModelVO(aiModelDao.queryByModelId(modelId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void modelInsert(ModelManageRequest request) {
        aiModelDao.insert(toModelPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void modelUpdate(ModelManageRequest request) {
        aiModelDao.update(toModelPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void modelDelete(Long id) {
        aiModelDao.delete(id);
    }

    // -------------------- MCP --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = McpVO.class, cacheType = CacheType.LIST)
    public List<McpVO> mcpPage(McpPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiMcp> poList = aiMcpDao.page(request.getIdKeyword(), request.getNameKeyword(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toMcpVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer mcpCount(McpPageRequest request) {
        return aiMcpDao.count(request.getIdKeyword(), request.getNameKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = McpVO.class, cacheType = CacheType.VALUE)
    public McpVO mcpQuery(Long id) {
        return toMcpVO(aiMcpDao.queryById(id));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = McpVO.class, cacheType = CacheType.VALUE)
    public McpVO mcpQuery(String mcpId) {
        return toMcpVO(aiMcpDao.queryByMcpId(mcpId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void mcpInsert(McpManageRequest request) {
        aiMcpDao.insert(toMcpPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void mcpUpdate(McpManageRequest request) {
        aiMcpDao.update(toMcpPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void mcpDelete(Long id) {
        aiMcpDao.delete(id);
    }

    // -------------------- Advisor --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = AdvisorVO.class, cacheType = CacheType.LIST)
    public List<AdvisorVO> advisorPage(AdvisorPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiAdvisor> poList = aiAdvisorDao.page(request.getIdKeyword(), request.getNameKeyword(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdvisorVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer advisorCount(AdvisorPageRequest request) {
        return aiAdvisorDao.count(request.getIdKeyword(), request.getNameKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = AdvisorVO.class, cacheType = CacheType.VALUE)
    public AdvisorVO advisorQuery(Long id) {
        return toAdvisorVO(aiAdvisorDao.queryById(id));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = AdvisorVO.class, cacheType = CacheType.VALUE)
    public AdvisorVO advisorQuery(String advisorId) {
        return toAdvisorVO(aiAdvisorDao.queryByAdvisorId(advisorId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void advisorInsert(AdvisorManageRequest request) {
        aiAdvisorDao.insert(toAdvisorPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void advisorUpdate(AdvisorManageRequest request) {
        aiAdvisorDao.update(toAdvisorPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void advisorDelete(Long id) {
        aiAdvisorDao.delete(id);
    }

    // -------------------- Prompt --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = PromptVO.class, cacheType = CacheType.LIST)
    public List<PromptVO> promptPage(PromptPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiPrompt> poList = aiPromptDao.page(request.getIdKeyword(), request.getNameKeyword(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toPromptVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer promptCount(PromptPageRequest request) {
        return aiPromptDao.count(request.getIdKeyword(), request.getNameKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = PromptVO.class, cacheType = CacheType.VALUE)
    public PromptVO promptQuery(Long id) {
        return toPromptVO(aiPromptDao.queryById(id));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = PromptVO.class, cacheType = CacheType.VALUE)
    public PromptVO promptQuery(String promptId) {
        return toPromptVO(aiPromptDao.queryByPromptId(promptId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void promptInsert(PromptManageRequest request) {
        aiPromptDao.insert(toPromptPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void promptUpdate(PromptManageRequest request) {
        aiPromptDao.update(toPromptPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void promptDelete(Long id) {
        aiPromptDao.delete(id);
    }

    // -------------------- Client --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = ClientVO.class, cacheType = CacheType.LIST)
    public List<ClientVO> clientPage(ClientPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiClient> poList = aiClientDao.page(request.getIdKeyword(), request.getNameKeyword(), request.getModelId(), request.getClientType(), request.getClientRole(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toClientVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer clientCount(ClientPageRequest request) {
        return aiClientDao.count(request.getIdKeyword(), request.getNameKeyword(), request.getModelId(), request.getClientType(), request.getClientRole());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = ClientVO.class, cacheType = CacheType.VALUE)
    public ClientVO clientQuery(Long id) {
        return toClientVO(aiClientDao.queryById(id));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = ClientVO.class, cacheType = CacheType.VALUE)
    public ClientVO clientQuery(String clientId) {
        return toClientVO(aiClientDao.queryByClientId(clientId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientInsert(ClientManageRequest request) {
        aiClientDao.insert(toClientPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientUpdate(ClientManageRequest request) {
        aiClientDao.update(toClientPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientDelete(Long id) {
        aiClientDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientToggle(Long id, Integer status) {
        AiClient po = AiClient.builder()
                .id(id)
                .clientStatus(status)
                .build();
        aiClientDao.toggle(po);
    }

    // -------------------- Agent --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.LIST)
    public List<AgentVO> agentPage(AgentPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiAgent> poList = aiAgentDao.page(request.getIdKeyword(), request.getNameKeyword(), request.getAgentType(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.LIST)
    public List<AgentVO> agentList(AgentListRequest request) {
        List<AiAgent> poList = aiAgentDao.list(request.getIdKeyword(), request.getNameKeyword(), request.getAgentType());
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer agentCount(AgentPageRequest request) {
        return aiAgentDao.count(request.getIdKeyword(), request.getNameKeyword(), request.getAgentType());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.VALUE)
    public AgentVO agentQuery(Long id) {
        return toAgentVO(aiAgentDao.queryById(id));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.VALUE)
    public AgentVO agentQuery(String agentId) {
        return toAgentVO(aiAgentDao.queryAgentByAgentId(agentId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentInsert(AgentManageRequest request) {
        aiAgentDao.insert(toAgentPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentUpdate(AgentManageRequest request) {
        aiAgentDao.update(toAgentPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentDelete(Long id) {
        aiAgentDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentToggle(Long id, Integer status) {
        AiAgent po = AiAgent.builder()
                .id(id)
                .agentStatus(status)
                .build();
        aiAgentDao.toggle(po);
    }

    // -------------------- User --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = UserVO.class, cacheType = CacheType.LIST)
    public List<UserVO> userPage(UserPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<User> poList = userDao.page(request.getUsernameKeyWord(), request.getRole(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toUserVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer userCount(UserPageRequest request) {
        Long count = userDao.count(request.getUsernameKeyWord(), request.getRole());
        return count == null ? 0 : count.intValue();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = UserVO.class, cacheType = CacheType.VALUE)
    public UserVO userQuery(Long id) {
        return toUserVO(userDao.queryById(id));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = UserVO.class, cacheType = CacheType.VALUE)
    public UserVO userQuery(String username) {
        return toUserVO(userDao.queryByUsername(username));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userInsert(UserManageRequest request) {
        userDao.insert(toUserPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userUpdate(UserManageRequest request) {
        userDao.update(toUserPo(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userDelete(Long id) {
        userDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userToggle(Long id, Integer status) {
        userDao.toggle(id, status);
    }

    // -------------------- Config --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_CONFIG_PREFIX, cacheType = CacheType.LIST, cacheClass = ConfigVO.class)
    public List<ConfigVO> configList(ConfigListRequest request) {
        List<AiConfig> poList = aiConfigDao.list(request.getIdKeyword(), request.getValueKeyword(), request.getConfigType());
        return poList.stream().map(this::toConfigVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CONFIG_PREFIX, cacheType = CacheType.VALUE, cacheClass = ConfigVO.class)
    public ConfigVO configQuery(ConfigManageRequest request) {
        return toConfigVO(aiConfigDao.queryByUniqueKey(request.getClientId(), request.getConfigType(), request.getConfigValue()));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CONFIG_PREFIX, cacheType = CacheType.VALUE, cacheClass = ConfigVO.class)
    public ConfigVO configQuery(Long id) {
        return toConfigVO(aiConfigDao.queryById(id));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configInsert(ConfigManageRequest request) {
        aiConfigDao.insert(toConfigPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configUpdate(ConfigManageRequest request) {
        aiConfigDao.update(toConfigPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configDelete(Long id) {
        aiConfigDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configToggle(Long id, Integer status) {
        aiConfigDao.toggle(id, status);
    }

    // -------------------- Flow --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_FLOW_PREFIX, cacheType = CacheType.LIST, cacheClass = ClientDetailVO.class)
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
    @Cacheable(cachePrefix = ADMIN_FLOW_PREFIX, cacheType = CacheType.LIST, cacheClass = FlowVO.class)
    public List<FlowVO> flowAgent(String agentId) {
        List<AiFlow> poList = aiFlowDao.queryByAgentId(agentId);
        return poList.stream().map(this::toFlowVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_FLOW_PREFIX, cacheType = CacheType.VALUE, cacheClass = FlowVO.class)
    public FlowVO flowQuery(String agentId, String clientId) {
        return toFlowVO(aiFlowDao.queryByAgentIdAndClientId(agentId, clientId));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_FLOW_PREFIX, cacheType = CacheType.VALUE, cacheClass = FlowVO.class)
    public FlowVO flowQuery(Long id) {
        return toFlowVO(aiFlowDao.queryById(id));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowInsert(FlowManageRequest request) {
        aiFlowDao.insert(toFlowPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowUpdate(FlowManageRequest request) {
        aiFlowDao.update(toFlowPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowDelete(Long id) {
        aiFlowDao.delete(id);
    }

    // -------------------- Task --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.LIST, cacheClass = TaskVO.class)
    public List<TaskVO> taskPage(TaskPageRequest request) {
        Integer offset = (request.getPageNum() - 1) * request.getPageSize();
        List<AiTask> poList = aiTaskDao.page(request.getIdKeyword(), request.getAgentId(), offset, request.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toTaskVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.VALUE, cacheClass = Integer.class)
    public Integer taskCount(TaskPageRequest request) {
        return aiTaskDao.count(request.getIdKeyword(), request.getAgentId());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.VALUE, cacheClass = TaskVO.class)
    public TaskVO taskQuery(Long id) {
        return toTaskVO(aiTaskDao.queryById(id));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.VALUE, cacheClass = TaskVO.class)
    public TaskVO taskQuery(String taskId) {
        return toTaskVO(aiTaskDao.queryByTaskId(taskId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskInsert(TaskManageRequest request) {
        aiTaskDao.insert(toTaskPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskUpdate(TaskManageRequest request) {
        aiTaskDao.update(toTaskPO(request));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskDelete(Long id) {
        aiTaskDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskToggle(Long id, Integer status) {
        AiTask po = AiTask.builder()
                .id(id)
                .taskStatus(status)
                .build();
        aiTaskDao.toggle(po);
    }

    // -------------------- Session --------------------
    @Override
    public List<SessionVO> listSession() {
        List<Session> list = sessionDao.queryAll();
        return list.stream().map(this::toSessionVO).toList();
    }

    // -------------------- Depend --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryClientDependOnPrompt(String promptId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(PROMPT.getType(), promptId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryClientDependOnAdvisor(String advisorId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(ADVISOR.getType(), advisorId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryClientDependOnMcp(String mcpId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(MCP.getType(), mcpId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryModelDependOnApi(String apiId) {
        return aiModelDao.queryModelIdByApiId(apiId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryClientDependOnModel(String modelId) {
        return aiClientDao.queryClientIdByModelId(modelId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryAgentDependOnClient(String clientId) {
        return aiFlowDao.queryAgentIdByClientId(clientId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_LIST_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> listApiId() {
        return aiApiDao.listApiId();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_LIST_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
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

    private SessionVO toSessionVO(Session session) {
        if (session == null) {
            return null;
        }
        return SessionVO.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .sessionUser(session.getSessionUser())
                .sessionTitle(session.getSessionTitle())
                .sessionType(session.getSessionType())
                .createTime(session.getCreateTime())
                .build();
    }

}
