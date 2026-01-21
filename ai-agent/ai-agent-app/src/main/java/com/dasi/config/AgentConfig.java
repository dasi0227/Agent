package com.dasi.config;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.service.armory.factory.ArmoryDynamicContext;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import com.dasi.infrastructure.persistent.dao.IAiFlowDao;
import com.dasi.infrastructure.persistent.dao.IAiPromptDao;
import com.dasi.properties.AgentAutoProperties;
import com.dasi.properties.OpenAiProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Configuration
@EnableConfigurationProperties({OpenAiProperties.class, AgentAutoProperties.class})
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

    @Resource
    private AgentAutoProperties agentAutoProperties;

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        try {
            List<String> clientIdList = agentAutoProperties.getClientIdList();
            if (clientIdList == null || clientIdList.isEmpty()) {
                return;
            }

            loadPrompt(clientIdList);

            log.info("【初始化配置】AI 客户端自动装配：{}", agentAutoProperties.getClientIdList());

            StrategyHandler<ArmoryRequestEntity, ArmoryDynamicContext, String> armoryRootNode = armoryStrategyFactory.getArmoryRootNode();
            ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                    .requestType(CLIENT.getType())
                    .idList(clientIdList)
                    .build();
            ArmoryDynamicContext armoryDynamicContext = new ArmoryDynamicContext();

            armoryRootNode.apply(armoryRequestEntity, armoryDynamicContext);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Resource
    private IAiPromptDao aiPromptDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    private void loadPrompt(List<String> clientIdList) {

        Path systemPromptDir = Path.of("docs", "prompt", "system-prompt");
        Path userPromptDir = Path.of("docs", "prompt", "user-prompt");

        if (!Files.isDirectory(systemPromptDir) || !Files.isDirectory(userPromptDir)) {
            log.error("【初始化配置】Prompt 文件夹不存在：systemPromptDir={}, userPromptDir={}", systemPromptDir.getFileName(), userPromptDir.getFileName());
            throw new RuntimeException();
        }

        for (String clientId : clientIdList) {
            try {

                // 解析 promptId 和 fileName
                String promptId = "prompt_" + clientId.substring("client_".length());
                String fileName = clientId + ".txt";

                // 更新 systemPrompt
                Path systemPromptFile = systemPromptDir.resolve(fileName);
                String systemPromptContent = Files.readString(systemPromptFile, StandardCharsets.UTF_8);
                aiPromptDao.loadPromptContent(promptId, systemPromptContent);

                // 更新 userPrompt
                Path userPromptFile = userPromptDir.resolve(fileName);
                String userPromptContent = Files.readString(userPromptFile, StandardCharsets.UTF_8);
                aiFlowDao.loadFlowPrompt(clientId, userPromptContent);

                log.error("【初始化配置】加载 prompt：clientId={}", clientId);

            } catch (Exception e) {
                log.error("【初始化配置】加载 prompt 失败：clientId={}", clientId, e);
                throw new RuntimeException();
            }
        }

    }

}
