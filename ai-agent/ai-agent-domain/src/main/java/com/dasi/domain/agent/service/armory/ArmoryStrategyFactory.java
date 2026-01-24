package com.dasi.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.service.armory.node.ArmoryRootNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ArmoryStrategyFactory {

    @Resource
    private ArmoryRootNode armoryRootNode;

    private final Map<String, IArmoryStrategy> type2StrategyMap = new ConcurrentHashMap<>();

    public ArmoryStrategyFactory(Map<String, IArmoryStrategy> armoryStrategyMap) {

        for (Map.Entry<String, IArmoryStrategy> entry : armoryStrategyMap.entrySet()) {
            IArmoryStrategy armoryStrategy = entry.getValue();
            type2StrategyMap.put(armoryStrategy.getType(), armoryStrategy);
        }

    }

    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> getArmoryRootNode(){
        return armoryRootNode;
    }

    public IArmoryStrategy getArmoryStrategyByType(String type){
        return type2StrategyMap.get(type);
    }

}
