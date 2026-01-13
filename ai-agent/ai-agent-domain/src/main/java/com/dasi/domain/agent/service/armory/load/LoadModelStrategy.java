package com.dasi.domain.agent.service.armory.load;

import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.model.vo.AiApiVO;
import com.dasi.domain.agent.model.vo.AiModelVO;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service("loadModelStrategy")
public class LoadModelStrategy implements ILoadStrategy {

    @Resource
    private IAgentRepository agentRepository;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        List<String> modelIdList = armoryCommandEntity.getCommandIdList();

        CompletableFuture<List<AiApiVO>> aiApiListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_api)：{}", modelIdList);
            return agentRepository.queryAiApiVOListByModelIdList(modelIdList);
        }, threadPoolExecutor);

        CompletableFuture<List<AiModelVO>> aiModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_model)：{}", modelIdList);
            return agentRepository.queryAiModelVOListByModelIdList(modelIdList);
        }, threadPoolExecutor);

    }

}
