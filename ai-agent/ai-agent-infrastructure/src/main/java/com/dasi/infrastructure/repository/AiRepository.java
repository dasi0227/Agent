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

        Set<AiApiVO> aiApiVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Client
            String clientKey = PO_CLIENT_PREFIX + clientId;
            AiClient aiClient = redisService.getStringValue(clientKey);
            if (aiClient == null) {
                aiClient = aiClientDao.queryByClientId(clientId);
                if (aiClient != null) redisService.setStringValue(clientKey, aiClient);
            }
            if (aiClient == null || aiClient.getClientStatus() == 0) continue;

            // 2. 获取 Model
            String modelId = aiClient.getModelId();
            String modelKey = PO_MODEL_PREFIX + modelId;
            AiModel aiModel = redisService.getStringValue(modelKey);
            if (aiModel == null) {
                aiModel = aiModelDao.queryByModelId(modelId);
                if (aiModel != null) redisService.setStringValue(modelKey, aiModel);
            }
            if (aiModel == null || aiModel.getModelStatus() == 0) continue;

            // 3. 获取 Api
            String apiId = aiModel.getApiId();
            String apiKey = PO_API_PREFIX + apiId;
            AiApi aiApi = redisService.getStringValue(apiKey);
            if (aiApi == null) {
                aiApi = aiApiDao.queryByApiId(apiId);
                if (aiApi != null) redisService.setStringValue(apiKey, aiApi);
            }
            if (aiApi == null || aiApi.getApiStatus() == 0) continue;

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

        return aiApiVOSet;
    }

    @Override
    public Set<AiModelVO> queryAiModelVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        Set<AiModelVO> aiModelVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Client
            String clientKey = PO_CLIENT_PREFIX + clientId;
            AiClient aiClient = redisService.getStringValue(clientKey);
            if (aiClient == null) {
                aiClient = aiClientDao.queryByClientId(clientId);
                if (aiClient != null) redisService.setStringValue(clientKey, aiClient);
            }
            if (aiClient == null || aiClient.getClientStatus() == 0) continue;

            // 2. 获取 Model
            String modelId = aiClient.getModelId();
            String modelKey = PO_MODEL_PREFIX + modelId;
            AiModel aiModel = redisService.getStringValue(modelKey);
            if (aiModel == null) {
                aiModel = aiModelDao.queryByModelId(modelId);
                if (aiModel != null) redisService.setStringValue(modelKey, aiModel);
            }
            if (aiModel == null || aiModel.getModelStatus() == 0) continue;

            // 3. 构造 VO
            AiModelVO aiModelVO = AiModelVO.builder()
                    .modelId(aiModel.getModelId())
                    .apiId(aiModel.getApiId())
                    .modelName(aiModel.getModelName())
                    .modelType(aiModel.getModelType())
                    .build();

            aiModelVOSet.add(aiModelVO);
        }

        return aiModelVOSet;
    }

    @Override
    public Set<AiAdvisorVO> queryAiAdvisorVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        Set<AiAdvisorVO> aiAdvisorVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Config
            String configKey = PO_CONFIG_PREFIX + clientId + ":" + ADVISOR.getType();
            List<AiConfig> clientConfigList = redisService.getStringValue(configKey);
            if (clientConfigList == null) {
                clientConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, ADVISOR.getType());
                if (clientConfigList != null) redisService.setStringValue(configKey, clientConfigList);
            }
            if (clientConfigList == null || clientConfigList.isEmpty()) continue;

            for (AiConfig clientConfig : clientConfigList) {

                String advisorId = clientConfig.getConfigValue();

                // 2. 获取 Advisor
                String advisorKey = PO_ADVISOR_PREFIX + advisorId;
                AiAdvisor aiAdvisor = redisService.getStringValue(advisorKey);
                if (aiAdvisor == null) {
                    aiAdvisor = aiAdvisorDao.queryByAdvisorId(advisorId);
                    if (aiAdvisor != null) redisService.setStringValue(advisorKey, aiAdvisor);
                }
                if (aiAdvisor == null || aiAdvisor.getAdvisorStatus() == 0) continue;

                // 3. 解析参数配置
                String advisorParam = aiAdvisor.getAdvisorParam();
                AiAdvisorVO.ChatMemory chatMemory = null;
                AiAdvisorVO.RagAnswer ragAnswer = null;

                if (advisorParam != null && !advisorParam.trim().isEmpty()) {
                    try {
                        switch (AiAdvisorType.fromCode(aiAdvisor.getAdvisorType())) {
                            case CHAT_MEMORY -> chatMemory = JSON.parseObject(advisorParam, AiAdvisorVO.ChatMemory.class);
                            case RAG_ANSWER -> ragAnswer = JSON.parseObject(advisorParam, AiAdvisorVO.RagAnswer.class);
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

        return aiAdvisorVOSet;
    }

    @Override
    public Map<String, AiPromptVO> queryAiPromptVOMapByClientIdSet(Set<String> clientIdSet) {

        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Map.of();
        }

        Map<String, AiPromptVO> aiPromptVOMap = new HashMap<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Config
            String configKey = PO_CONFIG_PREFIX + clientId + ":" + PROMPT.getType();
            List<AiConfig> clientConfigList = redisService.getStringValue(configKey);
            if (clientConfigList == null) {
                clientConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, PROMPT.getType());
                if (clientConfigList != null) redisService.setStringValue(configKey, clientConfigList);
            }
            if (clientConfigList == null || clientConfigList.isEmpty()) continue;

            for (AiConfig clientConfig : clientConfigList) {

                String promptId = clientConfig.getConfigValue();

                // 2. 获取 Prompt
                String promptKey = PO_PROMPT_PREFIX + promptId;
                AiPrompt aiPrompt = redisService.getStringValue(promptKey);
                if (aiPrompt == null) {
                    aiPrompt = aiPromptDao.queryByPromptId(promptId);
                    if (aiPrompt != null) redisService.setStringValue(promptKey, aiPrompt);
                }
                if (aiPrompt == null || aiPrompt.getPromptStatus() == 0) continue;

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

        return aiPromptVOMap;
    }

    @Override
    public Set<AiMcpVO> queryAiMcpVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        Set<AiMcpVO> aiMcpVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Config
            String configKey = PO_CONFIG_PREFIX + clientId + ":" + MCP.getType();
            List<AiConfig> clientConfigList = redisService.getStringValue(configKey);
            if (clientConfigList == null) {
                clientConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, MCP.getType());
                if (clientConfigList != null) redisService.setStringValue(configKey, clientConfigList);
            }
            if (clientConfigList == null || clientConfigList.isEmpty()) continue;

            for (AiConfig clientConfig : clientConfigList) {

                String mcpId = clientConfig.getConfigValue();

                // 2. 获取 MCP
                String mcpKey = PO_MCP_PREFIX + mcpId;
                AiMcp aiMcp = redisService.getStringValue(mcpKey);
                if (aiMcp == null) {
                    aiMcp = aiMcpDao.queryByMcpId(mcpId);
                    if (aiMcp != null) redisService.setStringValue(mcpKey, aiMcp);
                }
                if (aiMcp == null || aiMcp.getMcpStatus() == 0) continue;

                // 3. 构造 VO
                AiMcpVO aiMcpVO = AiMcpVO.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpType(aiMcp.getMcpType())
                        .mcpConfig(aiMcp.getMcpConfig())
                        .mcpTimeout(aiMcp.getMcpTimeout())
                        .build();

                try {
                    switch (AiMcpType.fromCode(aiMcp.getMcpType())) {
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

        return aiMcpVOSet;
    }

    @Override
    public Set<AiClientVO> queryAiClientVOSetByClientIdSet(Set<String> clientIdSet) {
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            return Set.of();
        }

        Set<AiClientVO> aiClientVOSet = new HashSet<>();

        for (String clientId : clientIdSet) {

            // 1. 获取 Client
            String clientKey = PO_CLIENT_PREFIX + clientId;
            AiClient aiClient = redisService.getStringValue(clientKey);
            if (aiClient == null) {
                aiClient = aiClientDao.queryByClientId(clientId);
                if (aiClient != null) redisService.setStringValue(clientKey, aiClient);
            }
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
                    .clientDesc(aiClient.getClientDesc())
                    .modelId(aiClient.getModelId())
                    .modelName(aiClient.getModelName())
                    .promptIdList(promptIdList)
                    .mcpIdList(mcpIdList)
                    .advisorIdList(advisorIdList)
                    .build();

            aiClientVOSet.add(aiClientVO);
        }

        return aiClientVOSet;
    }

    private List<String> extractConfigValueList(String clientId, String configType) {

        String configKey = PO_CONFIG_PREFIX + clientId + ":" + configType;
        List<AiConfig> clientConfigList = redisService.getStringValue(configKey);
        if (clientConfigList == null) {
            clientConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, configType);
            if (clientConfigList != null) redisService.setStringValue(configKey, clientConfigList);
        }
        if (clientConfigList == null || clientConfigList.isEmpty()) return List.of();

        List<String> configValueList = new ArrayList<>();
        for (AiConfig clientConfig : clientConfigList) {
            if (clientConfig.getConfigStatus() == 0) continue;
            configValueList.add(clientConfig.getConfigValue());
        }

        return configValueList;
    }

    @Override
    public Map<String, AiFlowVO> queryAiFlowVOMapByAgentId(String aiAgentId) {

        if (aiAgentId == null || aiAgentId.isEmpty()) {
            return Map.of();
        }

        // 获取 Flow
        String flowKey = PO_FLOW_PREFIX + aiAgentId;
        List<AiFlow> aiFlowList = redisService.getStringValue(flowKey);
        if (aiFlowList == null) {
            aiFlowList = aiFlowDao.queryByAgentId(aiAgentId);
            if (aiFlowList != null) redisService.setStringValue(flowKey, aiFlowList);
        }
        if (aiFlowList == null || aiFlowList.isEmpty()) return Map.of();

        Map<String, AiFlowVO> aiFlowVOMap = new HashMap<>();
        for (AiFlow aiFlow : aiFlowList) {
            AiFlowVO aiFlowVO = AiFlowVO.builder()
                    .agentId(aiFlow.getAgentId())
                    .clientId(aiFlow.getClientId())
                    .clientType(aiFlow.getClientType())
                    .flowPrompt(aiFlow.getFlowPrompt())
                    .flowSeq(aiFlow.getFlowSeq())
                    .build();

            aiFlowVOMap.put(aiFlow.getClientType(), aiFlowVO);
        }

        return aiFlowVOMap;
    }

    @Override
    public String queryExecuteTypeByAgentId(String aiAgentId) {

        String agentKey = PO_AGENT_PREFIX + aiAgentId;
        AiAgent aiAgent = redisService.getStringValue(agentKey);
        if (aiAgent == null) {
            aiAgent = aiAgentDao.queryAgentByAgentId(aiAgentId);
            if (aiAgent != null) redisService.setStringValue(agentKey, aiAgent);
        }
        if (aiAgent == null) return null;
        return aiAgent.getAgentType();

    }

    @Override
    public List<AiTaskVO> queryTaskVOList() {

        String taskKey = PO_TASK_PREFIX + "list";
        List<AiTask> aiTaskList = redisService.getStringValue(taskKey);
        if (aiTaskList == null) {
            aiTaskList = aiTaskDao.queryTaskList();
            if (aiTaskList != null) redisService.setStringValue(taskKey, aiTaskList);
        }
        if (aiTaskList == null || aiTaskList.isEmpty()) return List.of();

        return aiTaskList
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

}
