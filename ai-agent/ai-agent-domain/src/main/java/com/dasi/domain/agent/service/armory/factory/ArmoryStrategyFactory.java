package com.dasi.domain.agent.service.armory.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.service.armory.node.ArmoryRootNode;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ArmoryStrategyFactory {

    @Resource
    private ArmoryRootNode armoryRootNode;

    public StrategyHandler<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> getArmoryRootNode(){
        return armoryRootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }

    }

}
