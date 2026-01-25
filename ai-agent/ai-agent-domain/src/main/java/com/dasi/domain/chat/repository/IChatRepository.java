package com.dasi.domain.chat.repository;

import com.dasi.types.dto.response.ChatModelResponse;

import java.util.List;

public interface IChatRepository {

    List<ChatModelResponse> queryChatModelResponseList();

}
