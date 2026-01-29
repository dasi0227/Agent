package com.dasi.api;

import com.dasi.types.dto.response.AgentResponse;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.response.ChatMcpResponse;
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface IQueryApi {

    Result<List<ChatClientResponse>> queryChatClientResponseList();

    Result<List<ChatMcpResponse>> queryChatMcpResponseList();

    Result<List<String>> queryRagTagList();

    Result<List<AgentResponse>> queryAgentResponseList();

}
