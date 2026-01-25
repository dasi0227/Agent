package com.dasi.infrastructure.repository;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.enumeration.AiMcpType;
import com.dasi.domain.agent.model.enumeration.AiType;
import com.dasi.domain.agent.model.vo.*;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.dasi.domain.agent.model.enumeration.AiAdvisorType.CHAT_MEMORY;
import static com.dasi.domain.agent.model.enumeration.AiAdvisorType.RAG_ANSWER;
import static com.dasi.domain.agent.model.enumeration.AiType.*;

@Slf4j
@Repository
public class AgentRepository implements IAgentRepository {

    @Resource
    private IAiAdvisorDao advisorDao;

    @Resource
    private IAiApiDao apiDao;

    @Resource
    private IAiConfigDao configDao;

    @Resource
    private IAiClientDao clientDao;

    @Resource
    private IAiModelDao modelDao;

    @Resource
    private IAiPromptDao promptDao;

    @Resource
    private IAiMcpDao mcpDao;

    @Resource
    private IAiFlowDao flowDao;

    @Resource
    private IAiAgentDao agentDao;

    @Resource
    private IAiTaskDao taskDao;

    @Override
    public List<AiClientVO> queryAiClientVOListByClientIdList(List<String> clientIdList) {
        if (clientIdList == null || clientIdList.isEmpty()) {
            return List.of();
        }

        List<AiClientVO> aiClientVOList = new ArrayList<>();
        Set<String> aiClientIdSet = new HashSet<>();

        for (String clientId : clientIdList) {
            if (!aiClientIdSet.add(clientId)) {
                continue;
            }

            // 1. 查询客户端基本信息
            AiClient aiClient = clientDao.queryByClientId(clientId);
            if (aiClient == null || aiClient.getClientStatus() == 0) {
                continue;
            }

            // 2. 查询客户端关联配置
            List<AiConfig> clientConfigList = configDao.queryBySource(CLIENT.getType(), clientId);
            if (clientConfigList == null || clientConfigList.isEmpty()) {
                continue;
            }

            String modelId = null;
            List<String> promptIdList = new ArrayList<>();
            List<String> mcpIdList = new ArrayList<>();
            List<String> advisorIdList = new ArrayList<>();

            // 3. 根据 Config 拿到各个 Id
            for (AiConfig clientConfig : clientConfigList) {
                if (clientConfig.getConfigStatus() == 0) {
                    continue;
                }

                switch (AiType.fromType(clientConfig.getTargetType())) {
                    case MODEL:
                        modelId = clientConfig.getTargetId();
                        break;
                    case PROMPT:
                        promptIdList.add(clientConfig.getTargetId());
                        break;
                    case MCP:
                        mcpIdList.add(clientConfig.getTargetId());
                        break;
                    case ADVISOR:
                        advisorIdList.add(clientConfig.getTargetId());
                        break;
                }
            }

            // 4. 构建 VO
            AiClientVO aiClientVO = AiClientVO.builder()
                    .clientId(aiClient.getClientId())
                    .clientName(aiClient.getClientName())
                    .clientDesc(aiClient.getClientDesc())
                    .modelId(modelId)
                    .promptIdList(promptIdList)
                    .mcpIdList(mcpIdList)
                    .advisorIdList(advisorIdList)
                    .build();

            aiClientVOList.add(aiClientVO);
        }

        return aiClientVOList;
    }

    @Override
    public List<AiAdvisorVO> queryAiAdvisorVOListByClientIdList(List<String> clientIdList) {
        if (clientIdList == null || clientIdList.isEmpty()) {
            return List.of();
        }

        List<AiAdvisorVO> aiAdvisorVOList = new ArrayList<>();
        Set<String> aiAdvisorIdSet = new HashSet<>();

        for (String clientId : clientIdList) {

            List<AiConfig> clientConfigList = configDao.queryBySource(CLIENT.getType(), clientId);
            if (clientConfigList == null || clientConfigList.isEmpty()) {
                continue;
            }

            for (AiConfig clientConfig : clientConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!ADVISOR.getType().equals(clientConfig.getTargetType()) || clientConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 Advisor
                String advisorId = clientConfig.getTargetId();
                AiAdvisor aiAdvisor = advisorDao.queryByAdvisorId(advisorId);
                if (aiAdvisor == null || aiAdvisor.getAdvisorStatus() == 0) {
                    continue;
                }
                if (!aiAdvisorIdSet.add(aiAdvisor.getAdvisorId())) {
                    continue;
                }

                // 3. 解析参数配置
                String advisorParam = aiAdvisor.getAdvisorParam();
                AiAdvisorVO.ChatMemory chatMemory = null;
                AiAdvisorVO.RagAnswer ragAnswer = null;

                if (advisorParam != null && !advisorParam.trim().isEmpty()) {
                    try {
                        if (CHAT_MEMORY.getType().equals(aiAdvisor.getAdvisorType())) {
                            chatMemory = JSON.parseObject(advisorParam, AiAdvisorVO.ChatMemory.class);
                        } else if (RAG_ANSWER.getType().equals(aiAdvisor.getAdvisorType())) {
                            ragAnswer = JSON.parseObject(advisorParam, AiAdvisorVO.RagAnswer.class);
                        }
                    } catch (Exception e) {
                        log.error("【查询数据】失败：{}", e.getMessage());
                        throw new IllegalStateException(e);
                    }
                }

                // 4. 通过 Advisor 拿到 VO
                AiAdvisorVO aiAdvisorVO = AiAdvisorVO.builder()
                        .advisorId(aiAdvisor.getAdvisorId())
                        .advisorName(aiAdvisor.getAdvisorName())
                        .advisorType(aiAdvisor.getAdvisorType())
                        .advisorOrder(aiAdvisor.getAdvisorOrder())
                        .chatMemory(chatMemory)
                        .ragAnswer(ragAnswer)
                        .build();

                aiAdvisorVOList.add(aiAdvisorVO);
            }
        }

        return aiAdvisorVOList;
    }

    @Override
    public Map<String, AiPromptVO> queryAiPromptVOMapByClientIdList(List<String> clientIdList) {

        if (clientIdList == null || clientIdList.isEmpty()) {
            return Map.of();
        }

        Map<String, AiPromptVO> aiPromptVOMap = new HashMap<>();
        Set<String> aiPromptIdSet = new HashSet<>();

        for (String clientId : clientIdList) {

            List<AiConfig> clientConfigList = configDao.queryBySource(CLIENT.getType(), clientId);

            for (AiConfig clientConfig : clientConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!PROMPT.getType().equals(clientConfig.getTargetType()) || clientConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 Prompt
                String promptId = clientConfig.getTargetId();
                if (!aiPromptIdSet.add(promptId)) {
                    continue;
                }
                AiPrompt aiPrompt = promptDao.queryByPromptId(promptId);
                if (aiPrompt == null || aiPrompt.getPromptStatus() == 0) {
                    continue;
                }

                // 3. 通过 Prompt 拿到 VO
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
    public List<AiMcpVO> queryAiMcpVOListByClientIdList(List<String> clientIdList) {
        if (clientIdList == null || clientIdList.isEmpty()) {
            return List.of();
        }

        List<AiMcpVO> aiMcpVOList = new ArrayList<>();
        Set<String> aiMcpIdSet = new HashSet<>();

        for (String clientId : clientIdList) {

            List<AiConfig> clientConfigList = configDao.queryBySource(CLIENT.getType(), clientId);
            if (clientConfigList == null || clientConfigList.isEmpty()) {
                continue;
            }

            for (AiConfig clientConfig : clientConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!MCP.getType().equals(clientConfig.getTargetType()) || clientConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 MCP
                String mcpId = clientConfig.getTargetId();
                if (!aiMcpIdSet.add(mcpId)) {
                    continue;
                }

                // 3. 通过 Mcp 拿到 VO
                AiMcp aiMcp = mcpDao.queryByMcpId(mcpId);
                if (aiMcp == null || aiMcp.getMcpStatus() == 0) {
                    continue;
                }

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
                    aiMcpVOList.add(aiMcpVO);
                } catch (Exception e) {
                    log.error("【查询数据】失败：{}", e.getMessage());
                    throw new IllegalStateException(e);
                }
            }
        }

        return aiMcpVOList;
    }

    @Override
    public List<AiModelVO> queryAiModelVOListByClientIdList(List<String> clientIdList) {
        if (clientIdList == null || clientIdList.isEmpty()) {
            return List.of();
        }

        List<AiModelVO> aiModelVOList = new ArrayList<>();
        Set<String> aiModelIdSet = new HashSet<>();

        for (String clientId : clientIdList) {

            List<AiConfig> clientConfigList = configDao.queryBySource(CLIENT.getType(), clientId);
            if (clientConfigList == null || clientConfigList.isEmpty()) {
                continue;
            }

            for (AiConfig clientConfig : clientConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!MODEL.getType().equals(clientConfig.getTargetType()) || clientConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 Model
                String modelId = clientConfig.getTargetId();
                AiModel aiModel = modelDao.queryByModelId(modelId);
                if (aiModel == null || aiModel.getModelStatus() == 0) {
                    continue;
                }
                if (!aiModelIdSet.add(aiModel.getModelId())) {
                    continue;
                }

                // 3. 通过 Model 拿到 Mcp
                List<String> mcpIdList = new ArrayList<>();
                List<AiConfig> modelConfigList = configDao.queryBySource(MODEL.getType(), modelId);

                if (modelConfigList != null && !modelConfigList.isEmpty()) {
                    for (AiConfig modelConfig : modelConfigList) {
                        if (MCP.getType().equals(modelConfig.getTargetType()) && modelConfig.getConfigStatus() == 1) {
                            mcpIdList.add(modelConfig.getTargetId());
                        }
                    }
                }

                // 4. 通过 Model 拿到 VO
                AiModelVO aiModelVO = AiModelVO.builder()
                        .modelId(aiModel.getModelId())
                        .apiId(aiModel.getApiId())
                        .modelName(aiModel.getModelName())
                        .modelType(aiModel.getModelType())
                        .mcpIdList(mcpIdList)
                        .build();
                aiModelVOList.add(aiModelVO);
            }
        }

        return aiModelVOList;
    }

    @Override
    public List<AiApiVO> queryAiApiVOListByClientIdList(List<String> clientIdList) {
        if (clientIdList == null || clientIdList.isEmpty()) {
            return List.of();
        }

        List<AiApiVO> aiApiVOList = new ArrayList<>();
        Set<String> aiApiIdSet = new HashSet<>();

        for (String clientId : clientIdList) {

            List<AiConfig> clientConfigList = configDao.queryBySource(CLIENT.getType(), clientId);
            if (clientConfigList == null || clientConfigList.isEmpty()) {
                continue;
            }

            for (AiConfig clientConfig : clientConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!MODEL.getType().equals(clientConfig.getTargetType()) || clientConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 Model
                String modelId = clientConfig.getTargetId();
                AiModel aiModel = modelDao.queryByModelId(modelId);
                if (aiModel == null || aiModel.getModelStatus() == 0) {
                    continue;
                }

                // 3. 通过 Model 拿到 Api
                AiApi aiApi = apiDao.queryByApiId(aiModel.getApiId());
                if (aiApi == null || aiApi.getApiStatus() == 0) {
                    continue;
                }
                if (!aiApiIdSet.add(aiApi.getApiId())) {
                    continue;
                }

                // 4. 通过 API 拿到 VO
                AiApiVO aiApiVO = AiApiVO.builder()
                        .apiId(aiApi.getApiId())
                        .apiBaseUrl(aiApi.getApiBaseUrl())
                        .apiKey(aiApi.getApiKey())
                        .apiCompletionsPath(aiApi.getApiCompletionsPath())
                        .apiEmbeddingsPath(aiApi.getApiEmbeddingsPath())
                        .build();
                aiApiVOList.add(aiApiVO);
            }
        }

        return aiApiVOList;
    }

    @Override
    public List<AiModelVO> queryAiModelVOListByModelIdList(List<String> modelIdList) {
        if (modelIdList == null || modelIdList.isEmpty()) {
            return List.of();
        }

        List<AiModelVO> aiModelVOList = new ArrayList<>();
        Set<String> aiModelIdSet = new HashSet<>();

        for (String modelId : modelIdList) {
            AiModel aiModel = modelDao.queryByModelId(modelId);
            if (aiModel == null || aiModel.getModelStatus() == 0) {
                continue;
            }
            if (!aiModelIdSet.add(aiModel.getModelId())) {
                continue;
            }

            AiModelVO aiModelVO = AiModelVO.builder()
                    .modelId(aiModel.getModelId())
                    .apiId(aiModel.getApiId())
                    .modelName(aiModel.getModelName())
                    .modelType(aiModel.getModelType())
                    .build();
            aiModelVOList.add(aiModelVO);
        }

        return aiModelVOList;
    }

    @Override
    public List<AiApiVO> queryAiApiVOListByModelIdList(List<String> modelIdList) {
        if (modelIdList == null || modelIdList.isEmpty()) {
            return List.of();
        }

        List<AiApiVO> aiApiVOList = new ArrayList<>();
        Set<String> aiApiIdSet = new HashSet<>();

        for (String modelId : modelIdList) {
            AiModel aiModel = modelDao.queryByModelId(modelId);
            if (aiModel == null || aiModel.getModelStatus() == 0) {
                continue;
            }

            AiApi aiApi = apiDao.queryByApiId(aiModel.getApiId());
            if (aiApi == null || aiApi.getApiStatus() == 0) {
                continue;
            }
            if (!aiApiIdSet.add(aiApi.getApiId())) {
                continue;
            }

            AiApiVO aiApiVO = AiApiVO.builder()
                    .apiId(aiApi.getApiId())
                    .apiBaseUrl(aiApi.getApiBaseUrl())
                    .apiKey(aiApi.getApiKey())
                    .apiCompletionsPath(aiApi.getApiCompletionsPath())
                    .apiEmbeddingsPath(aiApi.getApiEmbeddingsPath())
                    .build();

            aiApiVOList.add(aiApiVO);
        }

        return aiApiVOList;
    }

    @Override
    public Map<String, AiFlowVO> queryAiFlowVOMapByAgentId(String aiAgentId) {

        if (aiAgentId == null || aiAgentId.isEmpty()) {
            return Map.of();
        }

        List<AiFlow> aiFlowList = flowDao.queryByAgentId(aiAgentId);

        if (aiFlowList == null || aiFlowList.isEmpty()) {
            return Map.of();
        }

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
        return agentDao.queryTypeByAgentId(aiAgentId);
    }

    @Override
    public List<AiTaskVO> queryTaskVOList() {

        return taskDao.queryTaskList()
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

    @Override
    public void updateTaskStatus(String taskId, Integer taskStatus) {
        taskDao.updateTaskStatus(taskId, taskStatus);
    }
}
