package com.dasi.test.domain;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiEnum.API;
import static com.dasi.domain.agent.model.enumeration.AiEnum.CLIENT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AgentTest {

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void test_aiClientApiNode() throws Exception {
        StrategyHandler<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler = armoryStrategyFactory.armoryStrategyHandler();

        ArmoryCommandEntity armoryCommandEntity = ArmoryCommandEntity.builder()
                .commandType(CLIENT.getCode())
                .commandIdList(List.of("client_demo_1"))
                .build();

        ArmoryStrategyFactory.DynamicContext dynamicContext = new ArmoryStrategyFactory.DynamicContext();

        String apply = armoryStrategyHandler.apply(armoryCommandEntity, dynamicContext);

        OpenAiApi openAiApi = (OpenAiApi) applicationContext.getBean(API.getBeanName("api_demo_1"));

        log.info("测试结果：{}", openAiApi);
    }


}
