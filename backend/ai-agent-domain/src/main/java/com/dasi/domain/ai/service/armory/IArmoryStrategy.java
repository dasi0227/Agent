package com.dasi.domain.ai.service.armory;

import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;

public interface IArmoryStrategy {

    void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext);

    String getType();

}
