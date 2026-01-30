package com.dasi.trigger.http;

import com.dasi.api.IQueryApi;
import com.dasi.domain.query.service.IQueryService;
import com.dasi.types.dto.response.WorkAgentResponse;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.response.ChatMcpResponse;
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
    public Result<List<ChatClientResponse>> queryChatClientResponseList() {
        List<ChatClientResponse> clientIdList = queryService.queryChatClientResponseList();
        return Result.success(clientIdList);
    }

    @GetMapping("/chat-mcp-list")
    @Override
    public Result<List<ChatMcpResponse>> queryChatMcpResponseList() {
        List<ChatMcpResponse> clientIdList = queryService.queryChatMcpResponseList();
        return Result.success(clientIdList);
    }

    @GetMapping("/rag-tag-list")
    @Override
    public Result<List<String>> queryRagTagList() {
        List<String> ragTagList = queryService.queryRagTagList();
        return Result.success(ragTagList);
    }

    @GetMapping("/agent-list")
    @Override
    public Result<List<WorkAgentResponse>> queryAgentResponseList() {
        List<WorkAgentResponse> workAgentResponseList = queryService.queryAgentResponseList();
        return Result.success(workAgentResponseList);
    }

}
