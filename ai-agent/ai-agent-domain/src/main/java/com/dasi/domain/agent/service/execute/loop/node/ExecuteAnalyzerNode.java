package com.dasi.domain.agent.service.execute.loop.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.agent.model.entity.ExecuteResponseEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.AbstractExecuteNode;
import com.dasi.domain.agent.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.ANALYZER;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.*;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "executeAnalyzerNode")
public class ExecuteAnalyzerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String analyzerJson;
        JSONObject analyzerObject;

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(ANALYZER.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient analyzerClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();

            String executionHistory = executeContext.getExecutionHistory().toString();
            if (executionHistory.isEmpty()) {
                executionHistory = "[暂无记录]";
            }

            String analyzerPrompt = String.format(flowPrompt,
                    executeContext.getRound(),
                    executeContext.getMaxRound(),
                    executeContext.getUserMessage(),
                    executeContext.getCurrentTask(),
                    executionHistory
            );

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
        parseAnalyzerResult(executeContext, analyzerObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeContext.setValue("analyzerResult", analyzerJson);

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

        ExecutePerformerNode executePerformerNode = getBean("executePerformerNode");
        ExecuteSummarizerNode executeSummarizerNode = getBean("executeSummarizerNode");

        if (executeContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteAnalyzerNode：任务已完成");
            return executeSummarizerNode;
        } else {
            return executePerformerNode;
        }

    }

    private void parseAnalyzerResult(ExecuteContext executeContext, JSONObject analyzerObject, String sessionId) {
        if (analyzerObject == null) {
            return;
        }
        sendAnalyzerResult(executeContext, ANALYZER.getExceptionType(), analyzerObject.getString(ANALYZER.getExceptionType()), sessionId);
        sendAnalyzerResult(executeContext, ANALYZER_DEMAND.getType(), analyzerObject.getString(ANALYZER_DEMAND.getType()), sessionId);
        sendAnalyzerResult(executeContext, ANALYZER_HISTORY.getType(), analyzerObject.getString(ANALYZER_HISTORY.getType()), sessionId);
        sendAnalyzerResult(executeContext, ANALYZER_STRATEGY.getType(), analyzerObject.getString(ANALYZER_STRATEGY.getType()), sessionId);
        sendAnalyzerResult(executeContext, ANALYZER_PROGRESS.getType(), analyzerObject.getString(ANALYZER_PROGRESS.getType()), sessionId);
        sendAnalyzerResult(executeContext, ANALYZER_STATUS.getType(), analyzerObject.getString(ANALYZER_STATUS.getType()), sessionId);
    }

    private void sendAnalyzerResult(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createAnalyzerResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getRound(),
                    sessionId
            );

            sendSseResult(executeContext, executeResponseEntity);
        }
    }

}
