package com.dasi.domain.chat.service.query;

import com.dasi.domain.chat.repository.IChatRepository;
import com.dasi.types.dto.response.ChatModelResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService implements IQueryService {

    @Resource
    private IChatRepository chatRepository;

    @Override
    public List<ChatModelResponse> queryChatModelResponseList() {
        return chatRepository.queryChatModelResponseList();
    }

}
