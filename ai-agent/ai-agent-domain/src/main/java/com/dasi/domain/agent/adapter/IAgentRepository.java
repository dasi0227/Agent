package com.dasi.domain.agent.adapter;

import com.dasi.domain.agent.model.vo.*;

import java.util.List;

public interface IAgentRepository {

    List<AiClientVO> queryAiClientVOListByClientIdList(List<String> clientIdList);

    List<AiAdvisorVO> queryAiAdvisorVOListByClientIdList(List<String> clientIdList);

    List<AiPromptVO> queryAiPromptVOListByClientIdList(List<String> clientIdList);

    List<AiMcpVO> queryAiMcpVOListByClientIdList(List<String> clientIdList);

    List<AiModelVO> queryAiModelVOListByClientIdList(List<String> clientIdList);

    List<AiApiVO> queryAiApiVOListByClientIdList(List<String> clientIdList);

    List<AiModelVO> queryAiModelVOListByModelIdList(List<String> modelIdList);

    List<AiApiVO> queryAiApiVOListByModelIdList(List<String> modelIdList);
}
