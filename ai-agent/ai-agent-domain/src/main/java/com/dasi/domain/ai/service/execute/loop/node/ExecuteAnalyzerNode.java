package com.dasi.domain.ai.service.execute.loop.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.ai.model.entity.ExecuteResponseEntity;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.vo.AiFlowVO;
import com.dasi.domain.ai.service.execute.AbstractExecuteNode;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.ai.model.enumeration.AiClientType.ANALYZER;
import static com.dasi.domain.ai.model.enumeration.AiClientType.PERFORMER;
import static com.dasi.domain.ai.model.enumeration.AiClientType.SUMMARIZER;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.*;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "analyzerNode")
public class ExecuteAnalyzerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String analyzerJson;
        JSONObject analyzerObject;

        String executionHistory = executeContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory = "[暂无记录]";
        }

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(ANALYZER.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient analyzerClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();
            String analyzerPrompt = flowPrompt.formatted(
                    executeContext.getRound(),
                    executeContext.getMaxRound(),
                    executeContext.getUserMessage(),
                    executeContext.getCurrentTask(),
                    executionHistory
            );

            // 获取客户端结果
            String analyzerResponse = analyzerClient
                    .prompt(analyzerPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                    .call()
                    .content();

            // 解析客户端结果
            analyzerJson = extractJson(analyzerResponse, "{}");
            analyzerObject = parseJsonObject(analyzerJson);
            if (analyzerObject == null) {
                throw new IllegalStateException("Analyzer 结果解析为空");
            }

        } catch (Exception e) {
            log.error("【执行节点】ExecuteAnalyzerNode：error={}", e.getMessage(), e);
            analyzerObject = buildExceptionObject(ANALYZER.getExceptionType(), e.getMessage());
            analyzerJson = analyzerObject.toJSONString();
        }

        log.info("\n=========================================== Analyzer ===========================================\n{}", analyzerJson);

        // 发送客户端结果
        parseAnalyzerResponse(executeContext, analyzerObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeContext.setValue(ANALYZER.getContextKey(), analyzerJson);

        // 检查客户端结果
        String analyzerStatus = analyzerObject.getString(ANALYZER_STATUS.getType());
        String analyzerProgress = analyzerObject.getString(ANALYZER_PROGRESS.getType());
        if ("COMPLETED".equalsIgnoreCase(analyzerStatus) || "100".equals(analyzerProgress)) {
            executeContext.setCompleted(true);
            return router(executeRequestEntity, executeContext);
        }

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        ExecutePerformerNode executePerformerNode = getBean(PERFORMER.getNodeName());
        ExecuteSummarizerNode executeSummarizerNode = getBean(SUMMARIZER.getNodeName());

        if (executeContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteAnalyzerNode：任务已完成");
            return executeSummarizerNode;
        } else {
            return executePerformerNode;
        }

    }

    private void parseAnalyzerResponse(ExecuteContext executeContext, JSONObject analyzerObject, String sessionId) {
        if (analyzerObject == null) {
            return;
        }
        sendAnalyzerResponse(executeContext, ANALYZER.getExceptionType(), analyzerObject.getString(ANALYZER.getExceptionType()), sessionId);
        sendAnalyzerResponse(executeContext, ANALYZER_DEMAND.getType(), analyzerObject.getString(ANALYZER_DEMAND.getType()), sessionId);
        sendAnalyzerResponse(executeContext, ANALYZER_HISTORY.getType(), analyzerObject.getString(ANALYZER_HISTORY.getType()), sessionId);
        sendAnalyzerResponse(executeContext, ANALYZER_STRATEGY.getType(), analyzerObject.getString(ANALYZER_STRATEGY.getType()), sessionId);
        sendAnalyzerResponse(executeContext, ANALYZER_PROGRESS.getType(), analyzerObject.getString(ANALYZER_PROGRESS.getType()), sessionId);
        sendAnalyzerResponse(executeContext, ANALYZER_STATUS.getType(), analyzerObject.getString(ANALYZER_STATUS.getType()), sessionId);
    }

    private void sendAnalyzerResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createAnalyzerResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getRound(),
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
