package com.dasi.config;

import com.dasi.domain.agent.service.dispatch.IDispatchService;
import com.dasi.infrastructure.persistent.dao.IAiConfigDao;
import com.dasi.infrastructure.persistent.dao.IAiFlowDao;
import com.dasi.infrastructure.persistent.dao.IAiPromptDao;
import com.dasi.properties.AgentProperties;
import com.dasi.properties.OpenAiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.*;

@Slf4j
@Configuration
@EnableConfigurationProperties({OpenAiProperties.class, AgentProperties.class})
public class AgentConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Bean
    @Primary
    @ConditionalOnBean(name = "postgresqlTemplate")
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
                .initializeSchema(false)
                .schemaName(openAiProperties.getEmbedding().getSchemaName())
                .vectorTableName(openAiProperties.getEmbedding().getTableName())
                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        log.info("【初始化配置】TokenTextSplitter");
        return new TokenTextSplitter();
    }

    @Autowired
    private AgentProperties agentProperties;

    @Autowired
    private IDispatchService dispatchService;

    @Autowired
    private IAiConfigDao configDao;

    @Autowired
    private IAiPromptDao promptDao;

    @Autowired
    private IAiFlowDao flowDao;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        loadPrompt();
        if (Boolean.TRUE.equals(agentProperties.getEnable())) {
            autoArmory(agentProperties.getAgentIdList(), AGENT.getType());
            autoArmory(agentProperties.getClientIdList(), CLIENT.getType());
        }
    }

    private void loadPrompt() {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<String> clientIdList = configDao.queryIdListByTargetType(PROMPT.getType());

        if (clientIdList == null || clientIdList.isEmpty()) {
            return;
        }

        for (String clientId : clientIdList) {
            try {

                // 解析 promptId 和 fileName
                String promptId = "prompt_" + clientId.substring("client_".length());
                String fileName = clientId + ".txt";

                // 解析文件路径
                Resource systemPromptFile = resolver.getResource("classpath:prompt/system-prompt/" + fileName);
                if (!systemPromptFile.exists()) {
                    log.error("【初始化配置】Prompt 文件不存在：{}", systemPromptFile.getDescription());
                    throw new IllegalStateException();
                }

                Resource userPromptFile = resolver.getResource("classpath:prompt/user-prompt/" + fileName);
                if (!userPromptFile.exists()) {
                    log.error("【初始化配置】Prompt 文件不存在：{}", userPromptFile.getDescription());
                    throw new IllegalStateException();
                }

                // 更新 Prompt
                String systemPromptContent = StreamUtils.copyToString(systemPromptFile.getInputStream(), StandardCharsets.UTF_8);
                promptDao.loadPromptContent(promptId, systemPromptContent);

                String userPromptContent = StreamUtils.copyToString(userPromptFile.getInputStream(), StandardCharsets.UTF_8);
                flowDao.loadFlowPrompt(clientId, userPromptContent);

                log.info("【初始化配置】加载 Prompt：clientId={}", clientId);

            } catch (Exception e) {
                log.error("【初始化配置】加载 prompt 失败：clientId={}", clientId, e);
                throw new IllegalStateException();
            }
        }
    }

    private void autoArmory(List<String> idList, String armoryType) {

        try {
            if (idList == null || idList.isEmpty()) return;
            dispatchService.dispatchArmoryStrategy(armoryType, idList);
        } catch (Exception e) {
            log.error("【初始化配置】自动装配客户端失败：error={}", e.getMessage(), e);
            throw new RuntimeException();
        }

    }

}
