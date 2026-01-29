package com.dasi.domain.ai.service.armory.strategy;

import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.vo.*;
import com.dasi.domain.ai.repository.IAiRepository;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import com.dasi.domain.ai.service.armory.IArmoryStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dasi.domain.ai.model.enumeration.AiArmoryType.ARMORY_CHAT;
import static com.dasi.domain.ai.model.enumeration.AiType.*;

@Slf4j
@Service("armoryChatStrategy")
public class ArmoryChatStrategy implements IArmoryStrategy {

    @Resource
    private IAiRepository aiRepository;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {

        Set<String> clientIdSet = armoryRequestEntity.getArmoryIdSet();
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            log.warn("【装配数据】Agent 装配：clientIdSet 为空");
            throw new IllegalStateException("装配数据时 clientIdSet 为空");
        }

        CompletableFuture<Set<AiApiVO>> aiApiSetFuture = CompletableFuture.supplyAsync(
                () -> aiRepository.queryAiApiVOSetByClientIdSet(clientIdSet), threadPoolExecutor);

        CompletableFuture<Set<AiModelVO>> aiModelSetFuture = CompletableFuture.supplyAsync(
                () -> aiRepository.queryAiModelVOSetByClientIdSet(clientIdSet), threadPoolExecutor);

        CompletableFuture<Set<AiMcpVO>> aiMcpSetFuture = CompletableFuture.supplyAsync(
                () -> aiRepository.queryAiMcpVOSetByClientIdSet(clientIdSet), threadPoolExecutor);

        CompletableFuture<Map<String, AiPromptVO>> aiPromptMapFuture = CompletableFuture.supplyAsync(
                () -> aiRepository.queryAiPromptVOMapByClientIdSet(clientIdSet), threadPoolExecutor);

        CompletableFuture<Set<AiAdvisorVO>> aiAdvisorSetFuture = CompletableFuture.supplyAsync(
                () -> aiRepository.queryAiAdvisorVOSetByClientIdSet(clientIdSet), threadPoolExecutor);

        CompletableFuture<Set<AiClientVO>> aiClientSetFuture = CompletableFuture.supplyAsync(
                () -> aiRepository.queryAiClientVOSetByClientIdSet(clientIdSet), threadPoolExecutor);

        CompletableFuture.allOf(
                aiApiSetFuture,
                aiModelSetFuture,
                aiMcpSetFuture,
                aiPromptMapFuture,
                aiAdvisorSetFuture,
                aiClientSetFuture
        ).join();

        Set<AiApiVO> aiApiSet = aiApiSetFuture.join();
        Set<AiModelVO> aiModelSet = aiModelSetFuture.join();
        Set<AiMcpVO> aiMcpSet = aiMcpSetFuture.join();
        Map<String, AiPromptVO> aiPrompMap = aiPromptMapFuture.join();
        Set<AiAdvisorVO> aiAdvisorSet = aiAdvisorSetFuture.join();
        Set<AiClientVO> aiClientSet = aiClientSetFuture.join();

        armoryContext.setValue(API.getType(), aiApiSet);
        armoryContext.setValue(MODEL.getType(), aiModelSet);
        armoryContext.setValue(MCP.getType(), aiMcpSet);
        armoryContext.setValue(PROMPT.getType(), aiPrompMap);
        armoryContext.setValue(ADVISOR.getType(), aiAdvisorSet);
        armoryContext.setValue(CLIENT.getType(), aiClientSet);

        log.info("【加载数据】ai_api_ids={}, size={}",
                aiApiSet.stream().map(AiApiVO::getApiId).toList(),
                aiApiSet.size());

        log.info("【加载数据】ai_model_ids={}, size={}",
                aiModelSet.stream().map(AiModelVO::getModelId).toList(),
                aiModelSet.size());

        log.info("【加载数据】ai_mcp_ids={}, size={}",
                aiMcpSet.stream().map(AiMcpVO::getMcpId).toList(),
                aiMcpSet.size());

        log.info("【加载数据】ai_prompt_ids={}, size={}",
                aiPrompMap.values().stream().map(AiPromptVO::getPromptId).toList(),
                aiPrompMap.size());

        log.info("【加载数据】ai_advisor_ids={}, size={}",
                aiAdvisorSet.stream().map(AiAdvisorVO::getAdvisorId).toList(),
                aiAdvisorSet.size());

        log.info("【加载数据】ai_client_ids={}, size={}",
                aiClientSet.stream().map(AiClientVO::getClientId).toList(),
                aiClientSet.size());

    }

    @Override
    public String getType() {
        return ARMORY_CHAT.getType();
    }

}
