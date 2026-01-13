package com.dasi.config;

import com.dasi.properties.OpenAiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class AgentConfig {

    @Bean
    @Primary
    public PgVectorStore pgVectorStore(OpenAiProperties openAiProperties, @Qualifier("postgresqlTemplate") JdbcTemplate jdbcTemplate) {

        log.info("【初始化配置】向量存储：PgVectorStore");

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(openAiProperties.getBaseUrl())
                .apiKey(openAiProperties.getApiKey())
                .build();

        OpenAiEmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
                .model(openAiProperties.getEmbedding().getModel())
                .dimensions(openAiProperties.getEmbedding().getDimensions())
                .encodingFormat(openAiProperties.getEmbedding().getEncodingFormat())
                .build();

        OpenAiEmbeddingModel openAiEmbeddingModel = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, embeddingOptions);

        return PgVectorStore.builder(jdbcTemplate, openAiEmbeddingModel)
                .initializeSchema(true)
                .schemaName(openAiProperties.getEmbedding().getSchemaName())
                .vectorTableName(openAiProperties.getEmbedding().getTableName())
                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        log.info("【初始化配置】分词器：tokenTextSplitter");
        return new TokenTextSplitter();
    }

}
