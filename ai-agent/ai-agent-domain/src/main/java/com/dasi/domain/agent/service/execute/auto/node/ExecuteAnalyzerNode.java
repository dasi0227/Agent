package com.dasi.domain.agent.service.execute.auto.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteCommandEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.auto.factory.AutoExecuteStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.ANALYZER;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service("executeAnalyzerNode")
public class ExecuteAnalyzerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        String analyzerPrompt = String.format("""
                你是 Analyzer 任务分析专家。
                请严格遵循 system prompt 的固定输出结构与字段名。
                参考信息：
                - 当前执行步骤：%s
                - 最大执行步骤：%s
                - 用户原始需求：%s
                - 当前任务需求：%s
                - 历史执行记录：%s
                """,
                dynamicContext.getStep(),
                dynamicContext.getMaxStep(),
                dynamicContext.getOriginalTask(),
                dynamicContext.getCurrentTask(),
                dynamicContext.getExecutionHistory().isEmpty() ? "[首次执行]" : dynamicContext.getExecutionHistory().toString()
        );

        AiFlowVO aiFlowVO = dynamicContext.getAiFlowVOMap().get(ANALYZER.getCode());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient analyzerClient = getBean(clientBeanName);

        String analyzerResult = analyzerClient
                .prompt(analyzerPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommandEntity.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .call()
                .content();

        dynamicContext.setValue("analyzerResult", analyzerResult);

        if (analyzerResult.contains("COMPLETED") || analyzerResult.contains("100%")) {
            dynamicContext.setCompleted(true);
            return router(executeCommandEntity, dynamicContext);
        }

        log.info("【执行节点】ExecuteAnalyzerNode：{}", analyzerResult);
        return router(executeCommandEntity, dynamicContext);

    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, AutoExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        ExecutePerformerNode executePerformerNode = getBean("executePerformerNode");
        ExecuteSummarizerNode executeSummarizerNode = getBean("executeSummarizerNode");

        if (dynamicContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteAnalyzerNode：任务已完成");
            return executeSummarizerNode;
        } else if (dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            log.info("【执行节点】ExecuteAnalyzerNode：任务已到达最大步数");
            return executeSummarizerNode;
        } else {
            return executePerformerNode;
        }

    }

}
