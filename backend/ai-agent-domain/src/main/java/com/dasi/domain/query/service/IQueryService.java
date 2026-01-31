package com.dasi.domain.query.service;

import com.dasi.types.dto.response.query.QueryChatRagResponse;
import com.dasi.types.dto.response.query.QueryWorkAgentResponse;
import com.dasi.types.dto.response.query.QueryChatClientResponse;
import com.dasi.types.dto.response.query.QueryChatMcpResponse;

import java.util.List;

public interface IQueryService {

    List<QueryChatClientResponse> queryChatClientResponseList();

    List<QueryChatMcpResponse> queryChatMcpResponseList();

    List<QueryChatRagResponse> queryChatRagList();

    List<QueryWorkAgentResponse> queryWorkAgentResponseList();

}
