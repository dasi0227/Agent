package com.dasi.domain.agent.service.execute.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteAutoResultEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.factory.ExecuteDynamicContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.ANALYZER;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.*;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service("executeAnalyzerNode")
public class ExecuteAnalyzerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {

        String executionHistory = executeDynamicContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory =  "[暂无记录]";
        }

        String analyzerPrompt = String.format("""
                你是一名专业的 Analyzer 任务分析专家。
                
                你需要基于提供的信息，深入分析需求，判断任务当前状态并制定明确的执行策略。
                
                分析要求：
                1. 理解用户真正想要什么
                2. 分析需要哪些具体的执行步骤
                3. 制定能够产生实际结果的执行策略
                4. 确保策略能够直接回答用户的问题
                
                参考信息：
                【当前执行步骤】%s
                【最大执行步骤】%s
                【用户原始需求】%s
                【当前任务需求】%s
                【历史执行记录】
                %s
                
                输出格式要求（必须严格遵守）：
                任务需求分析：[当前任务已完成部分与未完成部分的详细分析]
                执行历史评估：[对已完成工作的质量和效果评估]
                执行策略制定：[具体的执行计划，包括需要调用的工具]
                完成度评估：[0-100]%%
                任务状态：[CONTINUE/COMPLETED]
                """,
                executeDynamicContext.getStep(),
                executeDynamicContext.getMaxStep(),
                executeDynamicContext.getOriginalTask(),
                executeDynamicContext.getCurrentTask(),
                executionHistory
        );

        // 获取客户端
        AiFlowVO aiFlowVO = executeDynamicContext.getAiFlowVOMap().get(ANALYZER.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient analyzerClient = getBean(clientBeanName);

        // 获取客户端结果
        String analyzerResult = analyzerClient
                .prompt(analyzerPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .call()
                .content();

        // 解析客户端结果
        parseAnalyzerResult(executeDynamicContext, analyzerResult, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeDynamicContext.setValue("analyzerResult", analyzerResult);

        // 检查客户端结果
        if (analyzerResult.contains("COMPLETED") || analyzerResult.contains("100%")) {
            executeDynamicContext.setCompleted(true);
            return router(executeRequestEntity, executeDynamicContext);
        }

        return router(executeRequestEntity, executeDynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {

        ExecutePerformerNode executePerformerNode = getBean("executePerformerNode");
        ExecuteSummarizerNode executeSummarizerNode = getBean("executeSummarizerNode");

        if (executeDynamicContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteAnalyzerNode：任务已完成");
            return executeSummarizerNode;
        } else if (executeDynamicContext.getStep() > executeDynamicContext.getMaxStep()) {
            log.info("【执行节点】ExecuteAnalyzerNode：任务已到达最大步数");
            return executeSummarizerNode;
        } else {
            return executePerformerNode;
        }

    }

    private void parseAnalyzerResult(ExecuteDynamicContext executeDynamicContext, String analyzerResult, String sessionId) {

        String[] lines = analyzerResult.split("\n");
        String sectionType = "";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 每次都发送上一次积累的 section
            if (line.contains(ANALYZER_DEMAND.getName())) {
                log.info("【执行节点】ExecuteAnalyzerNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendAnalyzerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = ANALYZER_DEMAND.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(ANALYZER_HISTORY.getName())) {
                log.info("【执行节点】ExecuteAnalyzerNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendAnalyzerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = ANALYZER_HISTORY.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(ANALYZER_STRATEGY.getName())) {
                log.info("【执行节点】ExecuteAnalyzerNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendAnalyzerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = ANALYZER_STRATEGY.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(ANALYZER_PROGRESS.getName())) {
                log.info("【执行节点】ExecuteAnalyzerNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendAnalyzerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = ANALYZER_PROGRESS.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(ANALYZER_STATUS.getName())) {
                log.info("【执行节点】ExecuteAnalyzerNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendAnalyzerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = ANALYZER_STATUS.getType();
                sectionContent = new StringBuilder();
            } else {
                sectionContent.append(line).append("\n");
            }
        }

        // 发送最后的 section
        sendAnalyzerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
    }

    private void sendAnalyzerResult(ExecuteDynamicContext executeDynamicContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && !sectionContent.isEmpty()) {
            ExecuteAutoResultEntity executeAutoResultEntity = ExecuteAutoResultEntity.createAnalyzerResult(
                    sectionType,
                    sectionContent,
                    executeDynamicContext.getStep(),
                    sessionId
            );

            sendSseResult(executeDynamicContext, executeAutoResultEntity);
        }
    }

}
