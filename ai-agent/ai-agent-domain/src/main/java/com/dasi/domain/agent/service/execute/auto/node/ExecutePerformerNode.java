package com.dasi.domain.agent.service.execute.auto.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteCommandEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.auto.factory.AutoExecuteStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.PERFORMER;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecutePerformerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        String analyzerResult = dynamicContext.getValue("analyzerResult");
        if (analyzerResult == null) {
            analyzerResult = "";
        }

        String performerPrompt = String.format("""
                你是 Performer 任务执行专家。
                请严格遵循 system prompt 的固定输出结构与字段名。
                你需要使用 MCP 工具进行联网搜索。
                参考信息：
                - 用户原始需求：%s
                - 任务分析专家：%s
                """,
                dynamicContext.getOriginalTask(),
                analyzerResult.isEmpty() ? "[任务分析师异常，输出为空，请你自行决定执行策略]" : analyzerResult
        );

        AiFlowVO aiFlowVO = dynamicContext.getAiFlowVOMap().get(PERFORMER.getCode());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient performerClient = getBean(clientBeanName);

        String performerResult = performerClient
                .prompt(performerPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommandEntity.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .call()
                .content();

        dynamicContext.setValue("performerResult", performerResult);

        log.info("【执行节点】ExecutePerformerNode：{}", performerResult);
        return router(executeCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, AutoExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("executeSupervisorNode");
    }

}
