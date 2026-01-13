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

        CompletableFuture<List<AiApiVO>> aiApiListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_api) {}", clientIdList);
            return agentRepository.queryAiApiVOListByClientIdList(clientIdList);
        }, threadPoolExecutor);

        CompletableFuture<List<AiModelVO>> aiModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_model) {}", clientIdList);
            return agentRepository.queryAiModelVOListByClientIdList(clientIdList);
        }, threadPoolExecutor);

        CompletableFuture<List<AiMcpVO>> aiMcpListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_mcp) {}", clientIdList);
            return agentRepository.queryAiMcpVOListByClientIdList(clientIdList);
        }, threadPoolExecutor);

        CompletableFuture<List<AiPromptVO>> aiPromptListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_prompt) {}", clientIdList);
            return agentRepository.queryAiPromptVOListByClientIdList(clientIdList);
        }, threadPoolExecutor);

        CompletableFuture<List<AiAdvisorVO>> aiAdvisorListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_advisor) {}", clientIdList);
            return agentRepository.queryAiAdvisorVOListByClientIdList(clientIdList);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientVO>> aiClientListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", clientIdList);
            return agentRepository.queryAiClientVOListByClientIdList(clientIdList);
        }, threadPoolExecutor);

    }


}
