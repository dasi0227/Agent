package com.dasi.domain.ai.repository;

import com.dasi.domain.ai.model.vo.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAiRepository {

    Set<AiClientVO> queryAiClientVOSetByClientIdSet(Set<String> clientIdSet);

    Set<AiAdvisorVO> queryAiAdvisorVOSetByClientIdSet(Set<String> clientIdSet);

    Map<String, AiPromptVO> queryAiPromptVOMapByClientIdSet(Set<String> clientIdSet);

    Set<AiMcpVO> queryAiMcpVOSetByClientIdSet(Set<String> clientIdSet);

    Set<AiModelVO> queryAiModelVOSetByClientIdSet(Set<String> clientIdSet);

    Set<AiApiVO> queryAiApiVOSetByClientIdSet(Set<String> clientIdSet);

    Map<String, AiFlowVO> queryAiFlowVOMapByAgentId(String agentId);

    String queryExecuteTypeByAgentId(String agentId);

    List<AiTaskVO> queryTaskVOList();

    List<AiMcpVO> queryAiMcpVOListByMcpIdList(List<String> mcpIdList);
}
