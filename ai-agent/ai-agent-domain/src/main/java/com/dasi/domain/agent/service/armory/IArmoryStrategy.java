package com.dasi.domain.agent.service.armory;

import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;

public interface IArmoryStrategy {

    void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext dynamicContext);

}
