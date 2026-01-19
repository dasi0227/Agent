package com.dasi.domain.agent.service.execute.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteAutoResultEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.factory.ExecuteDynamicContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.SUMMARIZER;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.SUMMARIZER_OVERVIEW;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecuteSummarizerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {

        String analyzerResult = executeDynamicContext.getValue("analyzerResult");
        String performerResult = executeDynamicContext.getValue("performerResult");
        String supervisorResult = executeDynamicContext.getValue("supervisorResult");
        String executionHistory = executeDynamicContext.getExecutionHistory().toString();
        if (analyzerResult == null || analyzerResult.trim().isEmpty()) {
            analyzerResult =  "[任务分析专家异常，请你依据用户原始需求回答]";
        }
        if (performerResult == null || performerResult.trim().isEmpty()) {
            performerResult = "[任务执行专家异常，请你依据用户原始需求回答]";
        }
        if (supervisorResult == null || supervisorResult.trim().isEmpty()) {
            supervisorResult = "[任务监督专家异常，请你依据用户原始需求回答]";
        }
        if (executionHistory.isEmpty()) {
            executionHistory =  "[暂无记录]";
        }

        String summarizerPrompt = String.format("""
                你是一名专业的 Summarizer 任务总结专家。
                
                你需要基于提供的信息，根据用户需求、任务分析专家、任务执行专家和任务监督专家的输出，以及所有历史执行过程，直接给出交付到用户的最终回答。
                
                总结要求：
                1. 直接回答用户的原始问题
                2. 基于执行过程中获得的信息和结果
                3. 提供具体、实用的最终答案
                4. 如果是要求制定计划、列表等，请直接给出完整的内容
                5. 避免只描述执行过程，重点和终点是最终答案
                
                参考信息：
                【用户原始需求】%s
                【任务分析专家】
                %s
                【任务执行专家】
                %s
                【任务监督专家】
                %s
                【历史执行记录】
                %s
                
                输出格式要求（必须严格遵守）：
                [直接给出最终结果]
                """,
                executeDynamicContext.getOriginalTask(),
                analyzerResult,
                performerResult,
                supervisorResult,
                executionHistory
        );

        // 获取客户端
        AiFlowVO aiFlowVO = executeDynamicContext.getAiFlowVOMap().get(SUMMARIZER.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient summarizerClient = getBean(clientBeanName);

        // 获取客户端结果
        String summarizerResult = summarizerClient
                .prompt(summarizerPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId() + "-summary")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .call()
                .content();

        // 解析客户端结果
        parseSummarizerResult(executeDynamicContext, summarizerResult, executeRequestEntity.getSessionId());

        return router(executeRequestEntity, executeDynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

    private void parseSummarizerResult(ExecuteDynamicContext executeDynamicContext, String summarizerResult, String sessionId) {
        sendSummarizerResult(executeDynamicContext, SUMMARIZER_OVERVIEW.getType(), summarizerResult, sessionId);
//        sendCompleteResult(executeDynamicContext, sessionId);
    }

//    private void sendCompleteResult(ExecuteDynamicContext executeDynamicContext, String sessionId) {
//        ExecuteAutoResultEntity executeAutoResultEntity = ExecuteAutoResultEntity.createCompleteResult("执行完成", sessionId);
//        sendSseResult(executeDynamicContext, executeAutoResultEntity);
//    }

    private void sendSummarizerResult(ExecuteDynamicContext executeDynamicContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && !sectionContent.isEmpty()) {
            ExecuteAutoResultEntity executeAutoResultEntity = ExecuteAutoResultEntity.createSummarizerResult(
                    sectionType,
                    sectionContent,
                    executeDynamicContext.getStep(),
                    sessionId
            );

            sendSseResult(executeDynamicContext, executeAutoResultEntity);
        }
    }

}
