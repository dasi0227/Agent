package com.dasi.api;

import com.dasi.types.dto.request.ChatRequest;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.result.Result;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IChatService {

    String complete(ChatRequest chatRequest);

    Flux<String> stream(ChatRequest chatRequest);

    Result<List<ChatClientResponse>> queryChatClientResponseList();

}
