package com.dasi.domain.ai.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import com.dasi.domain.util.IRedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ArmoryRootNode extends AbstractArmoryNode {

    @Resource
    private ArmoryApiNode armoryApiNode;

    @Resource
    private IRedisService redisService;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {
        log.info("【装配节点】ArmoryRootNode：armoryType={}, armoryIdSet={}", armoryRequestEntity.getArmoryType(), armoryRequestEntity.getArmoryIdSet());
        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return armoryApiNode;
    }

}
