package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.enumeration.AiType;
import com.dasi.domain.agent.service.armory.factory.ArmoryDynamicContext;
import com.dasi.domain.agent.service.armory.strategy.IArmoryStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ArmoryRootNode extends AbstractArmoryNode {

    private final Map<String, IArmoryStrategy> loadStrategyMap;

    @Resource
    private ArmoryAiApiNode armoryAiApiNode;

    public ArmoryRootNode(Map<String, IArmoryStrategy> loadStrategyMap) {
        this.loadStrategyMap = loadStrategyMap;
    }

    @Override
    protected void multiThread(ArmoryRequestEntity armoryRequestEntity, ArmoryDynamicContext armoryDynamicContext) {
        String requestType = armoryRequestEntity.getRequestType();
        String loadStrategyKey = AiType.getLoadStrategyByCode(requestType);
        IArmoryStrategy loadStrategy = loadStrategyMap.get(loadStrategyKey);
        log.info("【加载数据】requestType={}", armoryRequestEntity.getRequestType());
        loadStrategy.armory(armoryRequestEntity, armoryDynamicContext);
    }

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryDynamicContext armoryDynamicContext) throws Exception {
        return router(armoryRequestEntity, armoryDynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryDynamicContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryDynamicContext armoryDynamicContext) {
        return armoryAiApiNode;
    }

}
