package com.dasi.test.domain;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.service.armory.model.ArmoryContext;
import com.dasi.domain.agent.service.armory.ArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.API;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ArmoryTest {

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void test_aiApiNode() throws Exception {
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryStrategyHandler = armoryStrategyFactory.getArmoryRootNode();

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(CLIENT.getType())
                .idList(List.of("client_demo_1"))
                .build();

        ArmoryContext dynamicContext = new ArmoryContext();

        armoryStrategyHandler.apply(armoryRequestEntity, dynamicContext);

        OpenAiApi openAiApi = (OpenAiApi) applicationContext.getBean(API.getBeanName("api_demo_1"));

        log.info("测试结果：{}", openAiApi);
    }

    @Test
    public void test_aiModelNode() throws Exception {
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryStrategyHandler = armoryStrategyFactory.getArmoryRootNode();

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(CLIENT.getType())
                .idList(List.of("client_demo_1"))
                .build();

        ArmoryContext dynamicContext = new ArmoryContext();

        armoryStrategyHandler.apply(armoryRequestEntity, dynamicContext);

        OpenAiChatModel chatModel = applicationContext.getBean(API.getBeanName("model_demo_1"), OpenAiChatModel.class);

        log.info("测试结果：{}", chatModel);

    }

    @Test
    public void test_aiClientNode() throws Exception {
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryStrategyHandler = armoryStrategyFactory.getArmoryRootNode();

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(CLIENT.getType())
                .idList(List.of("client_demo_1"))
                .build();

        ArmoryContext dynamicContext = new ArmoryContext();

        armoryStrategyHandler.apply(armoryRequestEntity, dynamicContext);

        ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName("client_demo_1"), ChatClient.class);

        log.info("测试结果：{}", chatClient);

    }

    @Test
    public void test_aiClientAskMath() throws Exception {
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryStrategyHandler = armoryStrategyFactory.getArmoryRootNode();

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(CLIENT.getType())
                .idList(List.of("client_demo_1"))
                .build();

        ArmoryContext dynamicContext = new ArmoryContext();

        armoryStrategyHandler.apply(armoryRequestEntity, dynamicContext);

        ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName("client_demo_1"), ChatClient.class);
        String answer = chatClient.prompt()
                .user("你是什么模型？然后告诉我1+1=?")
                .call()
                .content();

        log.info("测试结果：{}", answer);
    }

    @Test
    public void test_aiClientUseMcp() throws Exception {
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryStrategyHandler = armoryStrategyFactory.getArmoryRootNode();

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(CLIENT.getType())
                .idList(List.of("client_demo_1"))
                .build();

        ArmoryContext dynamicContext = new ArmoryContext();

        armoryStrategyHandler.apply(armoryRequestEntity, dynamicContext);

        ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName("client_demo_1"), ChatClient.class);
        String answer = chatClient.prompt()
                .user("""
                        你必须严格执行以下流程，并且所有步骤只能通过工具调用完成，禁止直接自然语言回答：
                        1) 调用工具 noticeArticle 发送企业微信应用消息：
                            - title：新年喜报
                            - description：祝大家新年快乐，万事如意！
                            - url：https://example.com
                        2) 调用工具  写入文件：
                            - 将上一步返回的 msgId 写到 Users/wyw/Downloads/ 下的 a.txt 之中
                            - 如果文件不存在则创建
                            - 必须使用绝对路径，且路径必须以我传递给你的路径开头，不要使用相对路径
                        3) 输出要求：
                            - 如果成功：只输出“成功”
                            - 如果失败：只输出失败原因
                            - 不允许输出任何额外解释或步骤
                        """)
                .call()
                .content();

        log.info("测试结果：{}", answer);
    }

    @Test
    public void test_aiClientUseRag() throws Exception {
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryStrategyHandler = armoryStrategyFactory.getArmoryRootNode();

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(CLIENT.getType())
                .idList(List.of("client_demo_1"))
                .build();

        ArmoryContext dynamicContext = new ArmoryContext();

        armoryStrategyHandler.apply(armoryRequestEntity, dynamicContext);

        ChatClient chatClient = applicationContext.getBean(CLIENT.getBeanName("client_demo_1"), ChatClient.class);
        String answer = chatClient.prompt()
                .user("Dasi 是什么大学的学生？Dasi 喜欢吃什么？你认为 Dasi 可能是哪里人？")
                .call()
                .content();

        log.info("测试结果：{}", answer);
    }


}
