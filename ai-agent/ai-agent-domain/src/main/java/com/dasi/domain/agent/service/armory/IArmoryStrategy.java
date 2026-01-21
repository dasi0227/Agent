package com.dasi.domain.agent.service.armory;

import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.service.armory.model.ArmoryContext;

public interface IArmoryStrategy {

    void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext dynamicContext);

}
