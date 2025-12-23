package com.dasi.http;

import com.dasi.IOllamaService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ollama")
public class OllamaController implements IOllamaService {

    @Resource
    private ChatClient chatClient;

    @GetMapping("/complete")
    @Override
    public ChatResponse generateComplete(@RequestParam String model, @RequestParam String message) {
        ChatOptions chatOptions = ChatOptions.builder().model(model).build();
        return chatClient
                .prompt()
                .user(message)
                .options(chatOptions)
                .call()
                .chatResponse();
    }

    @GetMapping("/stream")
    @Override
    public Flux<ChatResponse> generateStream(@RequestParam String model, @RequestParam String message) {
        ChatOptions chatOptions = ChatOptions.builder().model(model).build();
        return chatClient
                .prompt()
                .user(message)
                .options(chatOptions)
                .stream()
                .chatResponse();
    }

}
