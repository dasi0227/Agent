package com.dasi.trigger.http;

import com.dasi.api.IQueryApi;
import com.dasi.domain.query.service.IQueryService;
import com.dasi.types.dto.response.query.*;
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
        return Result.success(queryService.queryChatClientResponseList());
    }

    @GetMapping("/chat-mcp-list")
    @Override
    public Result<List<QueryChatMcpResponse>> queryChatMcpResponseList() {
        return Result.success(queryService.queryChatMcpResponseList());
    }

    @GetMapping("/chat-rag-list")
    @Override
    public Result<List<QueryChatRagResponse>> queryRagTagList() {
        return Result.success(queryService.queryChatRagList());
    }

    @GetMapping("/agent-list")
    @Override
    public Result<List<QueryWorkAgentResponse>> queryWorkAgentResponseList() {
        return Result.success(queryService.queryWorkAgentResponseList());
    }

}
