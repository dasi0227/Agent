package com.dasi.test.domain;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.armory.factory.ArmoryDynamicContext;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import com.dasi.domain.agent.service.execute.factory.ExecuteAutoStrategyFactory;
import com.dasi.domain.agent.service.execute.factory.ExecuteDynamicContext;
import com.dasi.infrastructure.persistent.dao.IAiPromptDao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ExecuteTest {

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Resource
    private ExecuteAutoStrategyFactory executeAutoStrategyFactory;

    @Resource
    private IAiPromptDao aiPromptDao;

    @Before
    public void updatePromptFromFiles() throws Exception {
        Path promptDir = Paths.get("/Users/wyw/Desktop/Project/Agent/ai-agent/docs/prompt");
        Map<String, String> promptFileMap = new LinkedHashMap<>();
        promptFileMap.put("prompt_analyzer_1", "analyzer.txt");
        promptFileMap.put("prompt_performer_1", "performer.txt");
        promptFileMap.put("prompt_supervisor_1", "supervisor.txt");
        promptFileMap.put("prompt_summarizer_1", "summarizer.txt");

        for (Map.Entry<String, String> entry : promptFileMap.entrySet()) {
            Path promptPath = promptDir.resolve(entry.getValue());
            String promptContent = Files.readString(promptPath, StandardCharsets.UTF_8);
            aiPromptDao.loadPromptContent(entry.getKey(), promptContent);
            log.info("更新 prompt 内容：prompt_id={}", entry.getKey());
        }
    }

    @Test
    public void test_autoAgent() throws Exception {
        StrategyHandler<ArmoryRequestEntity, ArmoryDynamicContext, String> armoryStrategyHandler = armoryStrategyFactory.getArmoryRootNode();

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .requestType(CLIENT.getType())
                .idList(Arrays.asList("client_analyzer_1", "client_performer_1", "client_supervisor_1", "client_summarizer_1"))
                .build();

        ArmoryDynamicContext armoryDynamicContext = new ArmoryDynamicContext();

        armoryStrategyHandler.apply(armoryRequestEntity, armoryDynamicContext);

        StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> executeHandler = executeAutoStrategyFactory.getExecuteRootNode();

        ExecuteRequestEntity executeRequestEntity = new ExecuteRequestEntity();
        executeRequestEntity.setAiAgentId("agent_demo_1");
        executeRequestEntity.setUserMessage("请总结今日热度最高的 AI 新闻。要求：输出中文，至少 3 条，每条包含标题、2-3 条要点和来源链接；信息必须来自可追溯网络来源；若无法检索或生成失败请说明原因");
        executeRequestEntity.setSessionId("session-id-" + System.currentTimeMillis());
        executeRequestEntity.setMaxStep(3);

        ExecuteDynamicContext executeDynamicContext = new ExecuteDynamicContext();

        executeHandler.apply(executeRequestEntity, executeDynamicContext);
    }

}
