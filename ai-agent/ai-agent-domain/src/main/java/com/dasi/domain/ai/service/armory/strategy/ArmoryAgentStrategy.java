package com.dasi.domain.ai.service.armory.strategy;

import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.vo.AiFlowVO;
import com.dasi.domain.ai.repository.IAiRepository;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import com.dasi.domain.ai.service.armory.IArmoryStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.dasi.domain.ai.model.enumeration.AiArmoryType.ARMORY_AGENT;

@Slf4j
@Service("armoryAgentStrategy")
public class ArmoryAgentStrategy implements IArmoryStrategy {

    @Resource
    private IAiRepository aiRepository;

    @Resource
    private ArmoryChatStrategy armoryChatStrategy;

    @Override
    public void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {

        Set<String> agentIdSet = armoryRequestEntity.getArmoryIdSet();
        if (agentIdSet == null || agentIdSet.isEmpty()) {
            log.warn("【装配数据】Agent 装配：agentIdSet 为空");
            throw new IllegalStateException("装配数据时 agentIdSet 为空");
        }

        Set<String> clientIdSet = new HashSet<>();
        for (String agentId : agentIdSet) {
            Map<String, AiFlowVO> flowMap = aiRepository.queryAiFlowVOMapByAgentId(agentId);
            if (flowMap == null || flowMap.isEmpty()) {
                continue;
            }
            for (AiFlowVO flowVO : flowMap.values()) {
                if (flowVO != null && flowVO.getClientId() != null && !flowVO.getClientId().isBlank()) {
                    clientIdSet.add(flowVO.getClientId());
                }
            }
        }

        if (clientIdSet.isEmpty()) {
            log.warn("【装配数据】Agent 装配：没有可用的 clientId");
            return;
        }

        ArmoryRequestEntity clientArmoryRequest = ArmoryRequestEntity.builder()
                .armoryType("client")
                .armoryIdSet(clientIdSet)
                .build();

        armoryChatStrategy.armory(clientArmoryRequest, armoryContext);
    }

    @Override
    public String getType() {
        return ARMORY_AGENT.getType();
    }

}
