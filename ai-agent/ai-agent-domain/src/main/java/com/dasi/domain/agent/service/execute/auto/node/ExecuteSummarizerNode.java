package com.dasi.domain.agent.service.execute.auto.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteCommandEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.auto.factory.AutoExecuteStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.SUMMARIZER;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecuteSummarizerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        String analyzerResult = dynamicContext.getValue("analyzerResult");
        String performerResult = dynamicContext.getValue("performerResult");
        String supervisorResult = dynamicContext.getValue("supervisorResult");
        if (analyzerResult == null) {
            analyzerResult = "";
        }
        if (performerResult == null) {
            performerResult = "";
        }
        if (supervisorResult == null) {
            supervisorResult = "";
        }

        String summarizerPrompt = String.format("""
                你是 Summarizer 任务总结专家。
                请严格遵循 system prompt 的固定输出结构与字段名。
                参考信息：
                - 用户原始需求：%s
                - 任务分析专家的输出：%s
                - 任务执行专家的输出：%s
                - 任务监督专家的输出：%s
                - 历史执行记录：%s
                """,
                dynamicContext.getOriginalTask(),
                analyzerResult,
                performerResult,
                supervisorResult,
                dynamicContext.getExecutionHistory().isEmpty() ? "[暂无记录]" : dynamicContext.getExecutionHistory().toString()
        );

        AiFlowVO aiFlowVO = dynamicContext.getAiFlowVOMap().get(SUMMARIZER.getCode());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient summarizerClient = getBean(clientBeanName);

        String summarizerResult = summarizerClient
                .prompt(summarizerPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommandEntity.getSessionId() + "-summary")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .call()
                .content();

        log.info("【执行节点】ExecuteSummarizerNode：{}", summarizerResult);
        return router(executeCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, AutoExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

}
