package com.dasi.domain.chat.service.query;

import com.dasi.types.dto.response.ChatClientResponse;

import java.util.List;

public interface IQueryService {

    List<ChatClientResponse> queryChatClientResponseList();

}
