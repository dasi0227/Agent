package com.dasi.domain.agent.service.execute.loop.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.agent.service.execute.loop.model.ExecuteLoopResult;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.loop.model.ExecuteLoopContext;
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
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteLoopContext executeLoopContext) throws Exception {

        // 获取客户端
        AiFlowVO aiFlowVO = executeLoopContext.getAiFlowVOMap().get(ANALYZER.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient analyzerClient = getBean(clientBeanName);

        // 获取提示词
        String flowPrompt = aiFlowVO.getFlowPrompt();

        String executionHistory = executeLoopContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory =  "[暂无记录]";
        }

        String analyzerPrompt = String.format(flowPrompt,
                executeLoopContext.getStep(),
                executeLoopContext.getMaxStep(),
                executeLoopContext.getOriginalTask(),
                executeLoopContext.getCurrentTask(),
                executionHistory
        );

        String analyzerJson;
        JSONObject analyzerObject;

        try {
            // 获取客户端结果
            String analyzerResult = analyzerClient
                    .prompt(analyzerPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 32768))
                    .call()
                    .content();

            // 解析客户端结果
            analyzerJson = extractJson(analyzerResult);
            analyzerObject = parseJsonObject(analyzerJson);
            if (analyzerObject == null) {
                throw new IllegalStateException("Analyzer 结果解析为空");
            }

        } catch (Exception e) {
            log.error("【执行节点】ExecuteAnalyzerNode：error={}", e.getMessage(), e);
            analyzerObject = buildExceptionResult(ANALYZER.getExceptionType(), e.getMessage());
            analyzerJson = analyzerObject.toJSONString();
        }

        log.info("\n=========================================== Analyzer ===========================================\n{}", analyzerJson);
        parseAnalyzerResult(executeLoopContext, analyzerObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeLoopContext.setValue("analyzerResult", analyzerJson);

        // 检查客户端结果
        String analyzerStatus = analyzerObject.getString(ANALYZER_STATUS.getType());
        String analyzerProgress = analyzerObject.getString(ANALYZER_PROGRESS.getType());
        if ("COMPLETED".equalsIgnoreCase(analyzerStatus) || "100".equals(analyzerProgress)) {
            executeLoopContext.setCompleted(true);
            return router(executeRequestEntity, executeLoopContext);
        }

        return router(executeRequestEntity, executeLoopContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteLoopContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteLoopContext executeLoopContext) throws Exception {

        ExecutePerformerNode executePerformerNode = getBean("executePerformerNode");
        ExecuteSummarizerNode executeSummarizerNode = getBean("executeSummarizerNode");

        if (executeLoopContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteAnalyzerNode：任务已完成");
            return executeSummarizerNode;
        } else {
            return executePerformerNode;
        }

    }

    private void parseAnalyzerResult(ExecuteLoopContext executeLoopContext, JSONObject analyzerObject, String sessionId) {
        if (analyzerObject == null) {
            return;
        }
        sendAnalyzerResult(executeLoopContext, ANALYZER.getExceptionType(), analyzerObject.getString(ANALYZER.getExceptionType()), sessionId);
        sendAnalyzerResult(executeLoopContext, ANALYZER_DEMAND.getType(), analyzerObject.getString(ANALYZER_DEMAND.getType()), sessionId);
        sendAnalyzerResult(executeLoopContext, ANALYZER_HISTORY.getType(), analyzerObject.getString(ANALYZER_HISTORY.getType()), sessionId);
        sendAnalyzerResult(executeLoopContext, ANALYZER_STRATEGY.getType(), analyzerObject.getString(ANALYZER_STRATEGY.getType()), sessionId);
        sendAnalyzerResult(executeLoopContext, ANALYZER_PROGRESS.getType(), analyzerObject.getString(ANALYZER_PROGRESS.getType()), sessionId);
        sendAnalyzerResult(executeLoopContext, ANALYZER_STATUS.getType(), analyzerObject.getString(ANALYZER_STATUS.getType()), sessionId);
    }

    private void sendAnalyzerResult(ExecuteLoopContext executeLoopContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteLoopResult executeLoopResult = ExecuteLoopResult.createAnalyzerResult(
                    sectionType,
                    sectionContent,
                    executeLoopContext.getStep(),
                    sessionId
            );

            sendSseResult(executeLoopContext, executeLoopResult);
        }
    }

}
