package com.dasi.domain.query.service;

import com.dasi.types.dto.response.WorkAgentResponse;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.response.ChatMcpResponse;

import java.util.List;

public interface IQueryService {

    List<ChatClientResponse> queryChatClientResponseList();

    List<ChatMcpResponse> queryChatMcpResponseList();

    List<String> queryRagTagList();

    List<WorkAgentResponse> queryAgentResponseList();

}
