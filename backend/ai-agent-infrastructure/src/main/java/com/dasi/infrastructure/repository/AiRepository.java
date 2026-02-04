package com.dasi.infrastructure.repository;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.dasi.domain.ai.model.enumeration.AiAdvisorType;
import com.dasi.domain.ai.model.enumeration.AiMcpType;
import com.dasi.domain.ai.model.vo.*;
import com.dasi.domain.ai.repository.IAiRepository;
import com.dasi.domain.util.IRedisService;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.dasi.domain.ai.model.enumeration.AiType.*;
import static com.dasi.types.constant.RedisConstant.*;

@Slf4j
@Repository
public class AiRepository implements IAiRepository {

    @Resource
    private IAiAdvisorDao aiAdvisorDao;

    @Resource
    private IAiApiDao aiApiDao;

    @Resource
    private IAiConfigDao aiConfigDao;

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private IAiPromptDao aiPromptDao;

    @Resource
    private IAiMcpDao aiMcpDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IAiTaskDao aiTaskDao;

    @Resource
    private IRedisService redisService;

    @Override
    public Set<AiApiVO> queryAiApiVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        String cacheKey = buildCacheKey(AI_API_VO_PREFIX, clientIdSet);
        List<AiApiVO> cachedList = redisService.getList(cacheKey, AiApiVO.class);
        if (cachedList != null) {
            return new HashSet<>(cachedList);
        }

        Set<AiApiVO> aiApiVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Client
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            if (aiClient == null || aiClient.getClientStatus() == 0) continue;

            // 2. 获取 Model
            String modelId = aiClient.getModelId();
            AiModel aiModel = aiModelDao.queryByModelId(modelId);
            if (aiModel == null) continue;

            // 3. 获取 Api
            String apiId = aiModel.getApiId();
            AiApi aiApi = aiApiDao.queryByApiId(apiId);
            if (aiApi == null) continue;

            // 4. 构造 VO
            AiApiVO aiApiVO = AiApiVO.builder()
                    .apiId(aiApi.getApiId())
                    .apiBaseUrl(aiApi.getApiBaseUrl())
                    .apiKey(aiApi.getApiKey())
                    .apiCompletionsPath(aiApi.getApiCompletionsPath())
                    .apiEmbeddingsPath(aiApi.getApiEmbeddingsPath())
                    .build();

            aiApiVOSet.add(aiApiVO);
        }

        redisService.setList(cacheKey, new ArrayList<>(aiApiVOSet));
        return aiApiVOSet;
    }

    @Override
    public Set<AiModelVO> queryAiModelVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        String cacheKey = buildCacheKey(AI_MODEL_VO_PREFIX, clientIdSet);
        List<AiModelVO> cachedList = redisService.getList(cacheKey, AiModelVO.class);
        if (cachedList != null) {
            return new HashSet<>(cachedList);
        }

        Set<AiModelVO> aiModelVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Client
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            if (aiClient == null || aiClient.getClientStatus() == 0) continue;

            // 2. 获取 Model
            String modelId = aiClient.getModelId();
            AiModel aiModel = aiModelDao.queryByModelId(modelId);
            if (aiModel == null) continue;

            // 3. 构造 VO
            AiModelVO aiModelVO = AiModelVO.builder()
                    .modelId(aiModel.getModelId())
                    .apiId(aiModel.getApiId())
                    .modelName(aiModel.getModelName())
                    .modelType(aiModel.getModelType())
                    .build();

            aiModelVOSet.add(aiModelVO);
        }

        redisService.setList(cacheKey, new ArrayList<>(aiModelVOSet));
        return aiModelVOSet;
    }

    @Override
    public Set<AiAdvisorVO> queryAiAdvisorVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        String cacheKey = buildCacheKey(AI_ADVISOR_VO_PREFIX, clientIdSet);
        List<AiAdvisorVO> cachedList = redisService.getList(cacheKey, AiAdvisorVO.class);
        if (cachedList != null) {
            return new HashSet<>(cachedList);
        }

        Set<AiAdvisorVO> aiAdvisorVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Config
            List<AiConfig> clientConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, ADVISOR.getType());
            if (clientConfigList == null || clientConfigList.isEmpty()) continue;

            for (AiConfig clientConfig : clientConfigList) {

                String advisorId = clientConfig.getConfigValue();

                // 2. 获取 Advisor
                AiAdvisor aiAdvisor = aiAdvisorDao.queryByAdvisorId(advisorId);
                if (aiAdvisor == null) continue;

                // 3. 解析参数配置
                String advisorParam = aiAdvisor.getAdvisorParam();
                AiAdvisorVO.ChatMemory chatMemory = null;
                AiAdvisorVO.RagAnswer ragAnswer = null;

                if (advisorParam != null && !advisorParam.trim().isEmpty()) {
                    try {
                        switch (AiAdvisorType.fromString(aiAdvisor.getAdvisorType())) {
                            case MEMORY -> chatMemory = JSON.parseObject(advisorParam, AiAdvisorVO.ChatMemory.class);
                            case RAG -> ragAnswer = JSON.parseObject(advisorParam, AiAdvisorVO.RagAnswer.class);
                        }
                    } catch (Exception e) {
                        log.error("【查询数据】失败：{}", e.getMessage());
                        throw new IllegalStateException(e);
                    }
                }

                // 4. 构造 VO
                AiAdvisorVO aiAdvisorVO = AiAdvisorVO.builder()
                        .advisorId(aiAdvisor.getAdvisorId())
                        .advisorName(aiAdvisor.getAdvisorName())
                        .advisorType(aiAdvisor.getAdvisorType())
                        .advisorOrder(aiAdvisor.getAdvisorOrder())
                        .chatMemory(chatMemory)
                        .ragAnswer(ragAnswer)
                        .build();

                aiAdvisorVOSet.add(aiAdvisorVO);
            }
        }

        redisService.setList(cacheKey, new ArrayList<>(aiAdvisorVOSet));
        return aiAdvisorVOSet;
    }

    @Override
    public Map<String, AiPromptVO> queryAiPromptVOMapByClientIdSet(Set<String> clientIdSet) {

        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Map.of();
        }

        String cacheKey = buildCacheKey(AI_PROMPT_VO_PREFIX, clientIdSet);
        Map<String, AiPromptVO> cachedMap = redisService.getMap(cacheKey, AiPromptVO.class);
        if (cachedMap != null) {
            return cachedMap;
        }

        Map<String, AiPromptVO> aiPromptVOMap = new HashMap<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Config
            List<AiConfig> clientConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, PROMPT.getType());
            if (clientConfigList == null || clientConfigList.isEmpty()) continue;

            for (AiConfig clientConfig : clientConfigList) {

                String promptId = clientConfig.getConfigValue();

                // 2. 获取 Prompt
                AiPrompt aiPrompt = aiPromptDao.queryByPromptId(promptId);
                if (aiPrompt == null) continue;

                // 3. 构造 VO
                AiPromptVO aiPromptVO = AiPromptVO.builder()
                        .promptId(aiPrompt.getPromptId())
                        .promptName(aiPrompt.getPromptName())
                        .promptContent(aiPrompt.getPromptContent())
                        .promptDesc(aiPrompt.getPromptDesc())
                        .build();

                aiPromptVOMap.put(promptId, aiPromptVO);
            }
        }

        redisService.setMap(cacheKey, aiPromptVOMap);
        return aiPromptVOMap;
    }

    @Override
    public Set<AiMcpVO> queryAiMcpVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        String cacheKey = buildCacheKey(AI_MCP_VO_PREFIX, clientIdSet);
        List<AiMcpVO> cachedList = redisService.getList(cacheKey, AiMcpVO.class);
        if (cachedList != null) {
            return new HashSet<>(cachedList);
        }

        Set<AiMcpVO> aiMcpVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Config
            List<AiConfig> clientConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, MCP.getType());
            if (clientConfigList == null || clientConfigList.isEmpty()) continue;

            for (AiConfig clientConfig : clientConfigList) {

                String mcpId = clientConfig.getConfigValue();

                // 2. 获取 MCP
                AiMcp aiMcp = aiMcpDao.queryByMcpId(mcpId);
                if (aiMcp == null) continue;

                // 3. 构造 VO
                AiMcpVO aiMcpVO = AiMcpVO.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpType(aiMcp.getMcpType())
                        .mcpConfig(aiMcp.getMcpConfig())
                        .mcpTimeout(aiMcp.getMcpTimeout())
                        .build();

                try {
                    switch (AiMcpType.fromString(aiMcp.getMcpType())) {
                        case SSE -> {
                            ObjectMapper objectMapper = new ObjectMapper();
                            AiMcpVO.SseConfig sseConfig = objectMapper.readValue(aiMcp.getMcpConfig(), AiMcpVO.SseConfig.class);
                            aiMcpVO.setSseConfig(sseConfig);
                        }
                        case STDIO -> {
                            Map<String, AiMcpVO.StdioConfig.Stdio> stdio = JSON.parseObject(aiMcp.getMcpConfig(), new TypeReference<>() {
                            });
                            AiMcpVO.StdioConfig stdioConfig = new AiMcpVO.StdioConfig();
                            stdioConfig.setStdio(stdio);
                            aiMcpVO.setStdioConfig(stdioConfig);
                        }
                    }

                    aiMcpVOSet.add(aiMcpVO);
                } catch (Exception e) {
                    log.error("【查询数据】失败：{}", e.getMessage());
                    throw new IllegalStateException(e);
                }
            }
        }

        redisService.setList(cacheKey, new ArrayList<>(aiMcpVOSet));
        return aiMcpVOSet;
    }

    @Override
    public Set<AiClientVO> queryAiClientVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        String cacheKey = buildCacheKey(AI_CLIENT_VO_PREFIX, clientIdSet);
        List<AiClientVO> cachedList = redisService.getList(cacheKey, AiClientVO.class);
        if (cachedList != null) {
            return new HashSet<>(cachedList);
        }

        Set<AiClientVO> aiClientVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Client
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            if (aiClient == null || aiClient.getClientStatus() == 0) continue;

            // 2. 获取 Config
            List<String> promptIdList = extractConfigValueList(clientId, PROMPT.getType());
            List<String> mcpIdList = extractConfigValueList(clientId, MCP.getType());
            List<String> advisorIdList = extractConfigValueList(clientId, ADVISOR.getType());

            // 3. 构建 VO
            AiClientVO aiClientVO = AiClientVO.builder()
                    .clientId(aiClient.getClientId())
                    .clientName(aiClient.getClientName())
                    .clientType(aiClient.getClientType())
                    .clientRole(aiClient.getClientRole())
                    .clientDesc(aiClient.getClientDesc())
                    .modelId(aiClient.getModelId())
                    .modelName(aiClient.getModelName())
                    .promptIdList(promptIdList)
                    .mcpIdList(mcpIdList)
                    .advisorIdList(advisorIdList)
                    .build();

            aiClientVOSet.add(aiClientVO);
        }

        redisService.setList(cacheKey, new ArrayList<>(aiClientVOSet));
        return aiClientVOSet;
    }

    private List<String> extractConfigValueList(String clientId, String configType) {

        String configKey = AI_CONFIG_VALUE_PREFIX + clientId + ":" + configType;
        List<String> cachedList = redisService.getList(configKey, String.class);
        if (cachedList != null) {
            return cachedList;
        }

        List<AiConfig> clientConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, configType);
        if (clientConfigList == null || clientConfigList.isEmpty()) {
            redisService.setList(configKey, List.of());
            return List.of();
        }

        List<String> configValueList = new ArrayList<>();
        for (AiConfig clientConfig : clientConfigList) {
            if (clientConfig.getConfigStatus() == 0) continue;
            configValueList.add(clientConfig.getConfigValue());
        }

        redisService.setList(configKey, configValueList);
        return configValueList;
    }

    @Override
    public Map<String, AiFlowVO> queryAiFlowVOMapByAgentId(String agentId) {

        if (agentId == null || agentId.isEmpty()) {
            return Map.of();
        }

        String flowKey = AI_FLOW_VO_PREFIX + agentId;
        Map<String, AiFlowVO> cachedMap = redisService.getMap(flowKey, AiFlowVO.class);
        if (cachedMap != null) {
            return cachedMap;
        }

        // 获取 Flow
        List<AiFlow> aiFlowList = aiFlowDao.queryByAgentId(agentId);
        if (aiFlowList == null || aiFlowList.isEmpty()) {
            redisService.setMap(flowKey, Map.of());
            return Map.of();
        }

        Map<String, AiFlowVO> aiFlowVOMap = new HashMap<>();
        for (AiFlow aiFlow : aiFlowList) {
            AiFlowVO aiFlowVO = AiFlowVO.builder()
                    .agentId(aiFlow.getAgentId())
                    .clientId(aiFlow.getClientId())
                    .clientRole(aiFlow.getClientRole())
                    .flowPrompt(aiFlow.getFlowPrompt())
                    .flowSeq(aiFlow.getFlowSeq())
                    .build();

            aiFlowVOMap.put(aiFlow.getClientRole(), aiFlowVO);
        }

        redisService.setMap(flowKey, aiFlowVOMap);
        return aiFlowVOMap;
    }

    @Override
    public String queryExecuteTypeByAgentId(String agentId) {

        String agentKey = AI_AGENT_TYPE_PREFIX + agentId;
        String cachedType = redisService.getValue(agentKey, String.class);
        if (cachedType != null) {
            return cachedType;
        }

        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(agentId);
        if (aiAgent == null) return null;
        redisService.setValue(agentKey, aiAgent.getAgentType());
        return aiAgent.getAgentType();

    }

    @Override
    public List<AiTaskVO> queryTaskVOList() {

        List<AiTaskVO> cachedList = redisService.getList(AI_TASK_VO_LIST_KEY, AiTaskVO.class);
        if (cachedList != null) {
            return cachedList;
        }

        List<AiTask> aiTaskList = aiTaskDao.queryTaskList();
        if (aiTaskList == null || aiTaskList.isEmpty()) {
            redisService.setList(AI_TASK_VO_LIST_KEY, List.of());
            return List.of();
        }

        List<AiTaskVO> aiTaskVOList = aiTaskList
                .stream()
                .map(aiTask -> AiTaskVO.builder()
                        .agentId(aiTask.getAgentId())
                        .taskId(aiTask.getTaskId())
                        .taskCron(aiTask.getTaskCron())
                        .taskDesc(aiTask.getTaskDesc())
                        .taskParam(parseTaskParam(aiTask.getTaskParam(), aiTask.getTaskId()))
                        .taskStatus(aiTask.getTaskStatus())
                        .build())
                .toList();
        redisService.setList(AI_TASK_VO_LIST_KEY, aiTaskVOList);
        return aiTaskVOList;
    }

    @Override
    public List<AiMcpVO> queryAiMcpVOListByMcpIdList(List<String> mcpIdList) {

        if (mcpIdList == null || mcpIdList.isEmpty()) {
            return List.of();
        }

        String cacheKey = buildCacheKey(AI_MCP_VO_PREFIX, mcpIdList);
        List<AiMcpVO> cachedList = redisService.getList(cacheKey, AiMcpVO.class);
        if (cachedList != null) {
            return cachedList;
        }

        List<AiMcpVO> aiMcpVOList = new ArrayList<>();

        List<AiMcp> aiMcpList = aiMcpDao.queryByMcpIdList(mcpIdList);
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            redisService.setList(cacheKey, List.of());
            return aiMcpVOList;
        }

        for (AiMcp aiMcp : aiMcpList) {
            AiMcpVO aiMcpVO = AiMcpVO.builder()
                    .mcpId(aiMcp.getMcpId())
                    .mcpName(aiMcp.getMcpName())
                    .mcpType(aiMcp.getMcpType())
                    .mcpConfig(aiMcp.getMcpConfig())
                    .mcpTimeout(aiMcp.getMcpTimeout())
                    .build();

            try {
                switch (com.dasi.domain.query.model.enumeration.AiMcpType.fromCode(aiMcp.getMcpType())) {
                    case SSE -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        AiMcpVO.SseConfig sseConfig = objectMapper.readValue(aiMcp.getMcpConfig(), AiMcpVO.SseConfig.class);
                        aiMcpVO.setSseConfig(sseConfig);
                    }
                    case STDIO -> {
                        Map<String, AiMcpVO.StdioConfig.Stdio> stdio = JSON.parseObject(aiMcp.getMcpConfig(), new TypeReference<>() {});
                        AiMcpVO.StdioConfig stdioConfig = new AiMcpVO.StdioConfig();
                        stdioConfig.setStdio(stdio);
                        aiMcpVO.setStdioConfig(stdioConfig);
                    }
                }
                aiMcpVOList.add(aiMcpVO);
            } catch (Exception e) {
                log.error("【查询数据】失败：{}", e.getMessage());
                throw new IllegalStateException(e);
            }
        }

        redisService.setList(cacheKey, aiMcpVOList);
        return aiMcpVOList;
    }

    private AiTaskVO.TaskParam parseTaskParam(String taskParam, String taskId) {
        try {
            if (taskParam == null || taskParam.isBlank()) throw new IllegalStateException("taskParam 为空，taskId=" + taskId);
            return JSON.parseObject(taskParam, AiTaskVO.TaskParam.class);
        } catch (Exception e) {
            log.error("【查询数据】失败：{}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private String buildCacheKey(String prefix, Collection<String> ids) {
        List<String> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);
        return prefix + String.join(",", sorted);
    }

}
