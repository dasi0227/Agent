package com.dasi.domain.query.repository;

import com.dasi.types.dto.response.QueryChatClientResponse;
import com.dasi.types.dto.response.QueryChatMcpResponse;
import com.dasi.types.dto.response.QueryChatRagResponse;
import com.dasi.types.dto.response.QueryWorkAgentResponse;

import java.util.List;

public interface IQueryRepository {

    List<QueryChatClientResponse> queryChatClientResponseList();

    List<QueryChatRagResponse> queryChatRagList();

    List<QueryChatMcpResponse> queryChatMcpResponseList();

    List<QueryWorkAgentResponse> queryWorkAgentResponseList();

}
