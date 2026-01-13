package com.dasi.domain.agent.service.armory.load;

import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.model.vo.*;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dasi.domain.agent.model.enumeration.AiEnum.*;

@Slf4j
@Service("loadClientStrategy")
public class LoadClientStrategy implements ILoadStrategy {
    
    @Resource
    private IAgentRepository agentRepository;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        List<String> clientIdList = armoryCommandEntity.getCommandIdList();

        CompletableFuture<List<AiApiVO>> aiApiListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiApiVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiModelVO>> aiModelListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiModelVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiMcpVO>> aiMcpListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiMcpVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiPromptVO>> aiPromptListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiPromptVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiAdvisorVO>> aiAdvisorListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiAdvisorVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture<List<AiClientVO>> aiClientListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiClientVOListByClientIdList(clientIdList), threadPoolExecutor);

        CompletableFuture.allOf(
                aiApiListFuture,
                aiModelListFuture,
                aiMcpListFuture,
                aiPromptListFuture,
                aiAdvisorListFuture,
                aiClientListFuture
        ).join();

        List<AiApiVO> aiApiList = aiApiListFuture.join();
        List<AiModelVO> aiModelList = aiModelListFuture.join();
        List<AiMcpVO> aiMcpList = aiMcpListFuture.join();
        List<AiPromptVO> aiPromptList = aiPromptListFuture.join();
        List<AiAdvisorVO> aiAdvisorList = aiAdvisorListFuture.join();
        List<AiClientVO> aiClientList = aiClientListFuture.join();

        dynamicContext.setValue(API.getCode(), aiApiList);
        dynamicContext.setValue(MODEL.getCode(), aiModelList);
        dynamicContext.setValue(MCP.getCode(), aiMcpList);
        dynamicContext.setValue(PROMPT.getCode(), aiPromptList);
        dynamicContext.setValue(ADVISOR.getCode(), aiAdvisorList);
        dynamicContext.setValue(CLIENT.getCode(), aiClientList);

        log.info("【加载数据】ai_api={}", aiApiList);
        log.info("【加载数据】ai_model={}", aiModelList);
        log.info("【加载数据】ai_mcp={}", aiMcpList);
        log.info("【加载数据】ai_prompt={}", aiPromptList);
        log.info("【加载数据】ai_advisor={}", aiAdvisorList);
        log.info("【加载数据】ai_client={}", aiClientList);
    }

}
