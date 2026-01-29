package com.dasi.api;

import com.dasi.types.dto.request.ArmoryRequest;
import com.dasi.types.dto.request.ChatRequest;
import com.dasi.types.dto.request.ExecuteRequest;
import com.dasi.types.dto.result.Result;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

public interface IAiApi {

    SseEmitter execute(ExecuteRequest executeRequest);

    String complete(ChatRequest chatRequest);

    Flux<String> stream(ChatRequest chatRequest);

    Result<Void> armory(ArmoryRequest armoryRequest);

}
