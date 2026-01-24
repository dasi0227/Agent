package com.dasi.domain.agent.service.armory.strategy;

import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.vo.AiApiVO;
import com.dasi.domain.agent.model.vo.AiModelVO;
import com.dasi.domain.agent.service.armory.IArmoryStrategy;
import com.dasi.domain.agent.service.armory.ArmoryContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dasi.domain.agent.model.enumeration.AiType.API;
import static com.dasi.domain.agent.model.enumeration.AiType.MODEL;

@Slf4j
@Service("armoryModelStrategy")
public class ArmoryModelStrategy implements IArmoryStrategy {

    @Resource
    private IAgentRepository agentRepository;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {

        List<String> modelIdList = armoryRequestEntity.getIdList();
        if (modelIdList == null || modelIdList.isEmpty()) {
            log.warn("【装配数据】Agent 装配：modelIdList 为空");
            throw new IllegalStateException("装配数据时 modelIdList 为空");
        }

        CompletableFuture<List<AiApiVO>> aiApiListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiApiVOListByModelIdList(modelIdList), threadPoolExecutor);

        CompletableFuture<List<AiModelVO>> aiModelListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.queryAiModelVOListByModelIdList(modelIdList), threadPoolExecutor);

        CompletableFuture.allOf(
                aiApiListFuture,
                aiModelListFuture
        ).join();

        List<AiApiVO> aiApiList = aiApiListFuture.join();
        List<AiModelVO> aiModelList = aiModelListFuture.join();

        armoryContext.setValue(API.getType(), aiApiList);
        armoryContext.setValue(MODEL.getType(), aiModelList);

        log.info("【加载数据】ai_api_ids={}, size={}",
                aiApiList.stream().map(AiApiVO::getApiId).toList(),
                aiApiList.size());

        log.info("【加载数据】ai_model_ids={}, size={}",
                aiModelList.stream().map(AiModelVO::getModelId).toList(),
                aiModelList.size());

    }

    @Override
    public String getType() {
        return "model";
    }

}
