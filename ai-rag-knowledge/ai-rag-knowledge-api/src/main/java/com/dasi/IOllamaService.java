package com.dasi;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public interface IOllamaService {

    ChatResponse generateComplete(String model, String message);

    Flux<ChatResponse> generateStream(String model, String message);

}
