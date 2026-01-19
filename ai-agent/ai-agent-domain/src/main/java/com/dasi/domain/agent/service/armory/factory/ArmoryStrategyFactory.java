package com.dasi.domain.agent.service.armory.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
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

    public StrategyHandler<ArmoryRequestEntity, ArmoryDynamicContext, String> getArmoryRootNode(){
        return armoryRootNode;
    }

}
