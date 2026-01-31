package com.dasi.api;

import com.dasi.types.dto.response.QueryChatRagResponse;
import com.dasi.types.dto.response.QueryWorkAgentResponse;
import com.dasi.types.dto.response.QueryChatClientResponse;
import com.dasi.types.dto.response.QueryChatMcpResponse;
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface IQueryApi {

    Result<List<QueryChatClientResponse>> queryChatClientResponseList();

    Result<List<QueryChatMcpResponse>> queryChatMcpResponseList();

    Result<List<QueryChatRagResponse>> queryRagTagList();

    Result<List<QueryWorkAgentResponse>> queryWorkAgentResponseList();

}
