package com.dasi.domain.query.repository;

import com.dasi.types.dto.response.query.QueryChatClientResponse;
import com.dasi.types.dto.response.query.QueryChatMcpResponse;
import com.dasi.types.dto.response.query.QueryChatRagResponse;
import com.dasi.types.dto.response.query.QueryWorkAgentResponse;

import java.util.List;

public interface IQueryRepository {

    List<QueryChatClientResponse> queryChatClientResponseList();

    List<QueryChatRagResponse> queryChatRagList();

    List<QueryChatMcpResponse> queryChatMcpResponseList();

    List<QueryWorkAgentResponse> queryWorkAgentResponseList();

}
