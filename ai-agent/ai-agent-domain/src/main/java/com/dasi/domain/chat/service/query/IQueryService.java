package com.dasi.domain.chat.service.query;

import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.response.ChatMcpResponse;

import java.util.List;

public interface IQueryService {

    List<ChatClientResponse> queryChatClientResponseList();

    List<ChatMcpResponse> queryChatMcpResponseList();

    List<String> queryRagTagList();

}
