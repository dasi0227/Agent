package com.dasi.domain.query.repository;

import com.dasi.domain.query.model.vo.AiMcpVO;
import com.dasi.types.dto.response.WorkAgentResponse;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.response.ChatMcpResponse;

import java.util.List;

public interface IQueryRepository {

    List<ChatClientResponse> queryChatClientResponseList();

    List<String> queryRagTagList();

    List<ChatMcpResponse> queryChatMcpResponseList();

    List<AiMcpVO> queryAiMcpVOListByMcpIdList(List<String> mcpIdList);

    List<WorkAgentResponse> queryAgentResponseList();
}
