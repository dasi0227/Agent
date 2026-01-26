package com.dasi.domain.chat.repository;

import com.dasi.types.dto.response.ChatClientResponse;

import java.util.List;

public interface IChatRepository {

    List<ChatClientResponse> queryChatClientResponseList();

}
