package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "openai", ignoreInvalidFields = true)
public class OpenAiProperties {

    private String baseUrl;

    private String apiKey;

    private EmbeddingProperties embedding;

    @Data
    public static class EmbeddingProperties {

        private Integer dimensions;

        private String model;

        private String schemaName;

        private String tableName;

        private String encodingFormat;

    }

}
