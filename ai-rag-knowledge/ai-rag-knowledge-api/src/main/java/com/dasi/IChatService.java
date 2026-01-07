package com.dasi;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public interface IChatService {

    ChatResponse complete(String model, String message, String ragTag);

    Flux<ChatResponse> stream(String model, String message, String ragTag);

}
