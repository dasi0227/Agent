package com.dasi.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfig {

    @Value("${spring.ai.openai.base-url}")      String openAiBaseUrl;
    @Value("${spring.ai.openai.api-key}")       String openAiApiKey;
    @Value("${spring.ai.ollama.base-url}")      String ollamaBaseUrl;
    @Value("${spring.ai.embedding.base-url}")   String embeddingBaseUrl;
    @Value("${spring.ai.embedding.model}")      String embeddingModel;
    @Value("${spring.ai.embedding.num-batch}")  Integer embeddingnumBatch;

    @Bean("tokenTextSplitter")
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean("embeddingModel")
    public EmbeddingModel embeddingModel() {

        OllamaApi ollamaApi = new OllamaApi(embeddingBaseUrl);

        OllamaOptions defaultOptions = OllamaOptions.builder()
                .model(embeddingModel)
                .numBatch(embeddingnumBatch)
                .build();

        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(defaultOptions)
                .observationRegistry(ObservationRegistry.NOOP)
                .build();
    }

    @Bean("openAiChatModel")
    public OpenAiChatModel openAiChatModel() {

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .observationRegistry(ObservationRegistry.NOOP)
                .build();
    }

    @Bean("ollamaChatModel")
    public OllamaChatModel ollamaChatModel() {

        OllamaApi ollamaApi = new OllamaApi(ollamaBaseUrl);

        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .observationRegistry(ObservationRegistry.NOOP)
                .build();
    }

}