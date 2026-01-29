package com.dasi.domain.query.service;

import com.dasi.domain.query.repository.IQueryRepository;
import com.dasi.types.dto.response.AgentResponse;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.response.ChatMcpResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService implements IQueryService {

    @Resource
    private IQueryRepository queryRepository;

    @Override
    public List<ChatClientResponse> queryChatClientResponseList() {
        return queryRepository.queryChatClientResponseList();
    }

    @Override
    public List<ChatMcpResponse> queryChatMcpResponseList() {
        return queryRepository.queryChatMcpResponseList();
    }

    @Override
    public List<String> queryRagTagList() {
        return queryRepository.queryRagTagList();
    }

    @Override
    public List<AgentResponse> queryAgentResponseList() {
        return queryRepository.queryAgentResponseList();
    }

}
