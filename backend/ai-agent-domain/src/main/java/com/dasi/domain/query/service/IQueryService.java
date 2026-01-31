package com.dasi.domain.query.service;

import com.dasi.types.dto.response.QueryChatRagResponse;
import com.dasi.types.dto.response.QueryWorkAgentResponse;
import com.dasi.types.dto.response.QueryChatClientResponse;
import com.dasi.types.dto.response.QueryChatMcpResponse;

import java.util.List;

public interface IQueryService {

    List<QueryChatClientResponse> queryChatClientResponseList();

    List<QueryChatMcpResponse> queryChatMcpResponseList();

    List<QueryChatRagResponse> queryChatRagList();

    List<QueryWorkAgentResponse> queryWorkAgentResponseList();

}
