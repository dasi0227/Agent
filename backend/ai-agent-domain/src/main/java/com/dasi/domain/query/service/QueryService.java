package com.dasi.domain.query.service;

import com.dasi.domain.query.repository.IQueryRepository;
import com.dasi.types.dto.response.query.QueryChatRagResponse;
import com.dasi.types.dto.response.query.QueryWorkAgentResponse;
import com.dasi.types.dto.response.query.QueryChatClientResponse;
import com.dasi.types.dto.response.query.QueryChatMcpResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService implements IQueryService {

    @Resource
    private IQueryRepository queryRepository;

    @Override
    public List<QueryChatClientResponse> queryChatClientResponseList() {
        return queryRepository.queryChatClientResponseList();
    }

    @Override
    public List<QueryChatMcpResponse> queryChatMcpResponseList() {
        return queryRepository.queryChatMcpResponseList();
    }

    @Override
    public List<QueryChatRagResponse> queryChatRagList() {
        return queryRepository.queryChatRagList();
    }

    @Override
    public List<QueryWorkAgentResponse> queryWorkAgentResponseList() {
        return queryRepository.queryWorkAgentResponseList();
    }

}
