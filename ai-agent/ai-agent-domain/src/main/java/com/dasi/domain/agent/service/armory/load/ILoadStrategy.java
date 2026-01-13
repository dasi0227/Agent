package com.dasi.domain.agent.service.armory.load;

import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;

public interface ILoadStrategy {

    void loadData(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext);

}
