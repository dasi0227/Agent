package com.dasi.trigger.http;

import com.dasi.api.IQueryApi;
import com.dasi.domain.query.service.IQueryService;
import com.dasi.types.dto.response.QueryChatClientResponse;
import com.dasi.types.dto.response.QueryChatMcpResponse;
import com.dasi.types.dto.response.QueryChatRagResponse;
import com.dasi.types.dto.response.QueryWorkAgentResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/query")
public class QueryController implements IQueryApi {

    @Resource
    private IQueryService queryService;

    @GetMapping("/chat-client-list")
    @Override
    public Result<List<QueryChatClientResponse>> queryChatClientResponseList() {
        List<QueryChatClientResponse> clientIdList = queryService.queryChatClientResponseList();
        return Result.success(clientIdList);
    }

    @GetMapping("/chat-mcp-list")
    @Override
    public Result<List<QueryChatMcpResponse>> queryChatMcpResponseList() {
        List<QueryChatMcpResponse> clientIdList = queryService.queryChatMcpResponseList();
        return Result.success(clientIdList);
    }

    @GetMapping("/chat-rag-list")
    @Override
    public Result<List<QueryChatRagResponse>> queryRagTagList() {
        List<QueryChatRagResponse> ragTagList = queryService.queryChatRagList();
        return Result.success(ragTagList);
    }

    @GetMapping("/agent-list")
    @Override
    public Result<List<QueryWorkAgentResponse>> queryWorkAgentResponseList() {
        List<QueryWorkAgentResponse> queryWorkAgentResponseList = queryService.queryWorkAgentResponseList();
        return Result.success(queryWorkAgentResponseList);
    }

}
