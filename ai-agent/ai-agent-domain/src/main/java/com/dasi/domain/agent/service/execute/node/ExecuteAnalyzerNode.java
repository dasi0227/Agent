package com.dasi.domain.agent.service.execute.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
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

        // 获取客户端
        AiFlowVO aiFlowVO = executeDynamicContext.getAiFlowVOMap().get(ANALYZER.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient analyzerClient = getBean(clientBeanName);

        // 获取提示词
        String flowPrompt = aiFlowVO.getFlowPrompt();

        String executionHistory = executeDynamicContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory =  "[暂无记录]";
        }

        String analyzerPrompt = String.format(flowPrompt,
                executeDynamicContext.getStep(),
                executeDynamicContext.getMaxStep(),
                executeDynamicContext.getOriginalTask(),
                executeDynamicContext.getCurrentTask(),
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
        parseAnalyzerResult(executeDynamicContext, analyzerObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeDynamicContext.setValue("analyzerResult", analyzerJson);

        // 检查客户端结果
        String analyzerStatus = analyzerObject.getString(ANALYZER_STATUS.getType());
        String analyzerProgress = analyzerObject.getString(ANALYZER_PROGRESS.getType());
        if ("COMPLETED".equalsIgnoreCase(analyzerStatus) || "100".equals(analyzerProgress)) {
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

    private void parseAnalyzerResult(ExecuteDynamicContext executeDynamicContext, JSONObject analyzerObject, String sessionId) {
        if (analyzerObject == null) {
            return;
        }
        sendAnalyzerResult(executeDynamicContext, ANALYZER.getExceptionType(), analyzerObject.getString(ANALYZER.getExceptionType()), sessionId);
        sendAnalyzerResult(executeDynamicContext, ANALYZER_DEMAND.getType(), analyzerObject.getString(ANALYZER_DEMAND.getType()), sessionId);
        sendAnalyzerResult(executeDynamicContext, ANALYZER_HISTORY.getType(), analyzerObject.getString(ANALYZER_HISTORY.getType()), sessionId);
        sendAnalyzerResult(executeDynamicContext, ANALYZER_STRATEGY.getType(), analyzerObject.getString(ANALYZER_STRATEGY.getType()), sessionId);
        sendAnalyzerResult(executeDynamicContext, ANALYZER_PROGRESS.getType(), analyzerObject.getString(ANALYZER_PROGRESS.getType()), sessionId);
        sendAnalyzerResult(executeDynamicContext, ANALYZER_STATUS.getType(), analyzerObject.getString(ANALYZER_STATUS.getType()), sessionId);
    }

    private void sendAnalyzerResult(ExecuteDynamicContext executeDynamicContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
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
