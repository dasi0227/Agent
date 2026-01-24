package com.dasi.domain.agent.adapter;

import com.dasi.domain.agent.model.vo.*;

import java.util.List;
import java.util.Map;

public interface IAgentRepository {

    List<AiClientVO> queryAiClientVOListByClientIdList(List<String> clientIdList);

    List<AiAdvisorVO> queryAiAdvisorVOListByClientIdList(List<String> clientIdList);

    Map<String, AiPromptVO> queryAiPromptVOMapByClientIdList(List<String> clientIdList);

    List<AiMcpVO> queryAiMcpVOListByClientIdList(List<String> clientIdList);

    List<AiModelVO> queryAiModelVOListByClientIdList(List<String> clientIdList);

    List<AiApiVO> queryAiApiVOListByClientIdList(List<String> clientIdList);

    List<AiModelVO> queryAiModelVOListByModelIdList(List<String> modelIdList);

    List<AiApiVO> queryAiApiVOListByModelIdList(List<String> modelIdList);

    Map<String, AiFlowVO> queryAiFlowVOMapByAgentId(String aiAgentId);

    String queryExecuteTypeByAgentId(String aiAgentId);

    List<AiTaskVO> queryTaskVOList();

    void updateTaskStatus(String taskId, Integer taskStatus);

}
