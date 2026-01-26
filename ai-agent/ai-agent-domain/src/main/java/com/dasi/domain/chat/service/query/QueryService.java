package com.dasi.domain.chat.service.query;

import com.dasi.domain.chat.repository.IChatRepository;
import com.dasi.types.dto.response.ChatClientResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService implements IQueryService {

    @Resource
    private IChatRepository chatRepository;

    @Override
    public List<ChatClientResponse> queryChatClientResponseList() {
        return chatRepository.queryChatClientResponseList();
    }

    @Override
    public List<String> queryRagTagList() {
        return chatRepository.queryRagTagList();
    }

}
