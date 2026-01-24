package com.dasi.domain.agent.service.armory.strategy;

import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.armory.ArmoryContext;
import com.dasi.domain.agent.service.armory.IArmoryStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service("armoryAgentStrategy")
public class ArmoryAgentStrategy implements IArmoryStrategy {

    @Resource
    private IAgentRepository agentRepository;

    @Resource
    private ArmoryClientStrategy armoryClientStrategy;

    @Override
    public void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {

        List<String> agentIdList = armoryRequestEntity.getIdList();
        if (agentIdList == null || agentIdList.isEmpty()) {
            log.warn("【装配数据】Agent 装配：agentIdList 为空");
            throw new IllegalStateException("装配数据时 agentIdList 为空");
        }

        Set<String> clientIdSet = new HashSet<>();
        for (String agentId : agentIdList) {
            Map<String, AiFlowVO> flowMap = agentRepository.queryAiFlowVOMapByAgentId(agentId);
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

        List<String> clientIdList = new ArrayList<>(clientIdSet);
        ArmoryRequestEntity clientArmoryRequest = ArmoryRequestEntity.builder()
                .armoryType("client")
                .idList(clientIdList)
                .build();

        armoryClientStrategy.armory(clientArmoryRequest, armoryContext);
    }

    @Override
    public String getType() {
        return "agent";
    }

}
