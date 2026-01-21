package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.service.armory.model.ArmoryContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class ArmoryRootNode extends AbstractArmoryNode {

    @Resource
    private ArmoryAiApiNode armoryAiApiNode;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {
        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return armoryAiApiNode;
    }

}
