package com.dasi.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;

public abstract class AbstractArmorySupport extends AbstractMultiThreadStrategyRouter<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> {

    @Override
    protected void multiThread(ArmoryCommandEntity requestParameter, ArmoryStrategyFactory.DynamicContext dynamicContext) {}

}
