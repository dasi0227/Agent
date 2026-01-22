package com.dasi.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.service.armory.node.ArmoryRootNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ArmoryStrategyFactory {

    @Resource
    private ArmoryRootNode armoryRootNode;

    private final Map<String, IArmoryStrategy> type2StrategyMap = new HashMap<>();

    public ArmoryStrategyFactory(Map<String, IArmoryStrategy> armoryStrategyMap) {

        for (Map.Entry<String, IArmoryStrategy> entry : armoryStrategyMap.entrySet()) {

            if (entry.getKey().equals("armoryClientStrategy")) {
                type2StrategyMap.put("client", entry.getValue());
            } else if (entry.getKey().equals("armoryModelStrategy")) {
                type2StrategyMap.put("model", entry.getValue());
            } else {
                log.warn("【初始化配置】未知装配策略: {}", entry.getKey());
            }

        }

    }

    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> getArmoryRootNode(){
        return armoryRootNode;
    }

    public IArmoryStrategy getArmoryStrategy(String type){
        return type2StrategyMap.get(type);
    }

}
