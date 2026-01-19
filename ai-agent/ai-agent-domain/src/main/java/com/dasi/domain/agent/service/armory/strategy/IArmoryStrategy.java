package com.dasi.domain.agent.service.armory.strategy;

import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.service.armory.factory.ArmoryDynamicContext;

public interface IArmoryStrategy {

    void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryDynamicContext dynamicContext);

}
