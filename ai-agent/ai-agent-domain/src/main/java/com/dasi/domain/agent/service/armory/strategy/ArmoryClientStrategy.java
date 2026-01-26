package com.dasi.domain.agent.service.armory.strategy;

import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.vo.*;
import com.dasi.domain.agent.service.armory.IArmoryStrategy;
import com.dasi.domain.agent.service.armory.ArmoryContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dasi.domain.agent.model.enumeration.AiType.*;

@Slf4j
@Service("armoryClientStrategy")
public class ArmoryClientStrategy implements IArmoryStrategy {

    @Resource
    private IAgentRepository agentRepository;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {

        List<String> clientIdList = armoryRequestEntity.getIdList();
        if (clientIdList == null || clientIdList.isEmpty()) {
            log.warn("【装配数据】Agent 装配：clientIdList 为空");
            throw new IllegalStateException("装配数据时 clientIdList 为空");
        }

        CompletableFuture<List<AiApiVO>> aiApiListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiApiVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiModelVO>> aiModelListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiModelVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiMcpVO>> aiMcpListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiMcpVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<Map<String, AiPromptVO>> aiPromptMapFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiPromptVOMapByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiAdvisorVO>> aiAdvisorListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiAdvisorVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiClientVO>> aiClientListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiClientVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture.allOf(
                aiApiListFuture,
                aiModelListFuture,
                aiMcpListFuture,
                aiPromptMapFuture,
                aiAdvisorListFuture,
                aiClientListFuture
        ).join();

        List<AiApiVO> aiApiList = aiApiListFuture.join();
        List<AiModelVO> aiModelList = aiModelListFuture.join();
        List<AiMcpVO> aiMcpList = aiMcpListFuture.join();
        Map<String, AiPromptVO> aiPrompMap = aiPromptMapFuture.join();
        List<AiAdvisorVO> aiAdvisorList = aiAdvisorListFuture.join();
        List<AiClientVO> aiClientList = aiClientListFuture.join();

        armoryContext.setValue(API.getType(), aiApiList);
        armoryContext.setValue(MODEL.getType(), aiModelList);
        armoryContext.setValue(MCP.getType(), aiMcpList);
        armoryContext.setValue(PROMPT.getType(), aiPrompMap);
        armoryContext.setValue(ADVISOR.getType(), aiAdvisorList);
        armoryContext.setValue(CLIENT.getType(), aiClientList);

        log.info("【加载数据】ai_api_ids={}, size={}",
                aiApiList.stream().map(AiApiVO::getApiId).toList(),
                aiApiList.size());

        log.info("【加载数据】ai_model_ids={}, size={}",
                aiModelList.stream().map(AiModelVO::getModelId).toList(),
                aiModelList.size());

        log.info("【加载数据】ai_mcp_ids={}, size={}",
                aiMcpList.stream().map(AiMcpVO::getMcpId).toList(),
                aiMcpList.size());

        log.info("【加载数据】ai_prompt_ids={}, size={}",
                aiPrompMap.values().stream().map(AiPromptVO::getPromptId).toList(),
                aiPrompMap.size());

        log.info("【加载数据】ai_advisor_ids={}, size={}",
                aiAdvisorList.stream().map(AiAdvisorVO::getAdvisorId).toList(),
                aiAdvisorList.size());

        log.info("【加载数据】ai_client_ids={}, size={}",
                aiClientList.stream().map(AiClientVO::getClientId).toList(),
                aiClientList.size());

    }

    @Override
    public String getType() {
        return "client";
    }

}
