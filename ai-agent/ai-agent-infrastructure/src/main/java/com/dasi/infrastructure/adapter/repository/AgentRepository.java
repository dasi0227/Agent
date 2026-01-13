package com.dasi.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.enumeration.AiConfigType;
import com.dasi.domain.agent.model.vo.*;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dasi.domain.agent.model.enumeration.AiAdvisorType.CHAT_MEMORY;
import static com.dasi.domain.agent.model.enumeration.AiAdvisorType.RAG_ANSWER;
import static com.dasi.domain.agent.model.enumeration.AiConfigType.*;

@Slf4j
@Repository
public class AgentRepository implements IAgentRepository {

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
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            if (aiClient == null || aiClient.getClientStatus() == 0) {
                continue;
            }

            // 2. 查询客户端关联配置
            List<AiConfig> aiConfigList = aiConfigDao.queryBySource(CLIENT.getCode(), clientId);

            String modelId = null;
            List<String> promptIdList = new ArrayList<>();
            List<String> mcpIdList = new ArrayList<>();
            List<String> advisorIdList = new ArrayList<>();

            // 3. 根据 Config 拿到各个 Id
            for (AiConfig aiConfig : aiConfigList) {
                if (aiConfig.getConfigStatus() == 0) {
                    continue;
                }

                switch (AiConfigType.fromCode(aiConfig.getTargetType())) {
                    case MODEL:
                        modelId = aiConfig.getTargetId();
                        break;
                    case PROMPT:
                        promptIdList.add(aiConfig.getTargetId());
                        break;
                    case MCP:
                        mcpIdList.add(aiConfig.getTargetId());
                        break;
                    case ADVISOR:
                        advisorIdList.add(aiConfig.getTargetId());
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

            List<AiConfig> aiConfigList = aiConfigDao.queryBySource(CLIENT.getCode(), clientId);

            for (AiConfig aiConfig : aiConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!ADVISOR.getCode().equals(aiConfig.getTargetType()) || aiConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 Advisor
                String advisorId = aiConfig.getTargetId();
                AiAdvisor aiAdvisor = aiAdvisorDao.queryByAdvisorId(advisorId);
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
                        if (CHAT_MEMORY.getCode().equals(aiAdvisor.getAdvisorType())) {
                            chatMemory = JSON.parseObject(advisorParam, AiAdvisorVO.ChatMemory.class);
                        } else if (RAG_ANSWER.getCode().equals(aiAdvisor.getAdvisorType())) {
                            ragAnswer = JSON.parseObject(advisorParam, AiAdvisorVO.RagAnswer.class);
                        }
                    } catch (Exception ignored) {
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
    public List<AiPromptVO> queryAiPromptVOListByClientIdList(List<String> clientIdList) {
        if (clientIdList == null || clientIdList.isEmpty()) {
            return List.of();
        }

        List<AiPromptVO> aiPromptVOList = new ArrayList<>();
        Set<String> aiPromptIdSet = new HashSet<>();

        for (String clientId : clientIdList) {

            List<AiConfig> aiConfigList = aiConfigDao.queryBySource(CLIENT.getCode(), clientId);

            for (AiConfig aiConfig : aiConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!PROMPT.getCode().equals(aiConfig.getTargetType()) || aiConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 Prompt
                String promptId = aiConfig.getTargetId();
                AiPrompt aiPrompt = aiPromptDao.queryByPromptId(promptId);
                if (aiPrompt == null || aiPrompt.getPromptStatus() == 0) {
                    continue;
                }
                if (!aiPromptIdSet.add(aiPrompt.getPromptId())) {
                    continue;
                }

                // 3. 通过 Prompt 拿到 VO
                AiPromptVO aiPromptVO = AiPromptVO.builder()
                        .promptId(aiPrompt.getPromptId())
                        .promptName(aiPrompt.getPromptName())
                        .promptContent(aiPrompt.getPromptContent())
                        .promptDesc(aiPrompt.getPromptDesc())
                        .build();

                aiPromptVOList.add(aiPromptVO);
            }
        }

        return aiPromptVOList;
    }

    @Override
    public List<AiMcpVO> queryAiMcpVOListByClientIdList(List<String> clientIdList) {
        if (clientIdList == null || clientIdList.isEmpty()) {
            return List.of();
        }

        List<AiMcpVO> aiMcpVOList = new ArrayList<>();
        Set<String> aiMcpIdSet = new HashSet<>();

        for (String clientId : clientIdList) {

            List<AiConfig> aiConfigList = aiConfigDao.queryBySource(CLIENT.getCode(), clientId);

            for (AiConfig aiConfig : aiConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!MCP.getCode().equals(aiConfig.getTargetType()) || aiConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 MCP
                String mcpId = aiConfig.getTargetId();
                AiMcp aiMcp = aiMcpDao.queryByMcpId(mcpId);
                if (!aiMcpIdSet.add(aiMcp.getMcpId())) {
                    continue;
                }

                // 3. 通过 MCP 拿到 VO
                AiMcpVO aiMcpVO = AiMcpVO.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpType(aiMcp.getMcpType())
                        .mcpPath(aiMcp.getMcpPath())
                        .mcpTimeout(aiMcp.getMcpTimeout())
                        .build();
                aiMcpVOList.add(aiMcpVO);
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

            List<AiConfig> aiConfigList = aiConfigDao.queryBySource(CLIENT.getCode(), clientId);

            for (AiConfig aiConfig : aiConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!MODEL.getCode().equals(aiConfig.getTargetType()) || aiConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 Model
                String modelId = aiConfig.getTargetId();
                AiModel aiModel = aiModelDao.queryByModelId(modelId);
                if (aiModel == null || aiModel.getModelStatus() == 0) {
                    continue;
                }
                if (!aiModelIdSet.add(aiModel.getModelId())) {
                    continue;
                }

                // 3. 通过 Model 拿到 VO
                AiModelVO aiModelVO = AiModelVO.builder()
                        .modelId(aiModel.getModelId())
                        .apiId(aiModel.getApiId())
                        .modelName(aiModel.getModelName())
                        .modelType(aiModel.getModelType())
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

            List<AiConfig> aiConfigList = aiConfigDao.queryBySource(CLIENT.getCode(), clientId);

            for (AiConfig aiConfig : aiConfigList) {
                // 1. 通过 Client 拿到 Config
                if (!MODEL.getCode().equals(aiConfig.getTargetType()) || aiConfig.getConfigStatus() == 0) {
                    continue;
                }

                // 2. 通过 Config 拿到 Model
                String modelId = aiConfig.getTargetId();
                AiModel aiModel = aiModelDao.queryByModelId(modelId);
                if (aiModel == null || aiModel.getModelStatus() == 0) {
                    continue;
                }

                // 3. 通过 Model 拿到 Api
                AiApi aiApi = aiApiDao.queryByApiId(aiModel.getApiId());
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
            AiModel aiModel = aiModelDao.queryByModelId(modelId);
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
            AiModel aiModel = aiModelDao.queryByModelId(modelId);
            if (aiModel == null || aiModel.getModelStatus() == 0) {
                continue;
            }

            AiApi aiApi = aiApiDao.queryByApiId(aiModel.getApiId());
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
}
