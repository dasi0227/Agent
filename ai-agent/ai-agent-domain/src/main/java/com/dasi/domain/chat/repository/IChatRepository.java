package com.dasi.domain.chat.repository;

import com.dasi.types.dto.response.ModelResponse;

import java.util.List;

public interface IChatRepository {

    List<ModelResponse> queryModelResponseList();

}
