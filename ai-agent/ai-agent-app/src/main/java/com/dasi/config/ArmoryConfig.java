package com.dasi.config;

import com.dasi.domain.ai.service.dispatch.IDispatchService;
import com.dasi.infrastructure.persistent.dao.IAiConfigDao;
import com.dasi.infrastructure.persistent.dao.IAiFlowDao;
import com.dasi.infrastructure.persistent.dao.IAiPromptDao;
import com.dasi.properties.ArmoryProperties;
import com.dasi.properties.OpenAiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static com.dasi.domain.ai.model.enumeration.AiArmoryType.ARMORY_AGENT;
import static com.dasi.domain.ai.model.enumeration.AiArmoryType.ARMORY_CHAT;
import static com.dasi.domain.ai.model.enumeration.AiType.PROMPT;

@Slf4j
@Configuration
@EnableConfigurationProperties({OpenAiProperties.class, ArmoryProperties.class})
public class ArmoryConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ArmoryProperties armoryProperties;

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
        if (Boolean.TRUE.equals(armoryProperties.getEnable())) {
            autoArmory(armoryProperties.getAgentIdList(), ARMORY_AGENT.getType());
            autoArmory(armoryProperties.getClientIdList(), ARMORY_CHAT.getType());
        }
    }

    private void autoArmory(Set<String> armoryIdSet, String armoryType) {

        try {
            if (armoryIdSet == null || armoryIdSet.isEmpty()) return;
            dispatchService.dispatchArmoryStrategy(armoryType, armoryIdSet);
        } catch (Exception e) {
            log.error("【初始化配置】自动装配客户端失败：error={}", e.getMessage(), e);
            throw new RuntimeException();
        }

    }

    private void loadPrompt() {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<String> clientIdList = configDao.queryClientIdListByConfigType(PROMPT.getType());

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

}
