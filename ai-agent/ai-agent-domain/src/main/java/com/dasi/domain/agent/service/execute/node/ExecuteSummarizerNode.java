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

import static com.dasi.domain.agent.model.enumeration.AiClientType.SUMMARIZER;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.SUMMARIZER_OVERVIEW;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecuteSummarizerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {

        // 获取客户端
        AiFlowVO aiFlowVO = executeDynamicContext.getAiFlowVOMap().get(SUMMARIZER.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient summarizerClient = getBean(clientBeanName);

        // 获取提示词
        String flowPrompt = aiFlowVO.getFlowPrompt();

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

        String summarizerPrompt = String.format(flowPrompt,
                executeDynamicContext.getOriginalTask(),
                analyzerResult,
                performerResult,
                supervisorResult,
                executionHistory
        );

        // 获取客户端结果
        String summarizerResult = summarizerClient
                .prompt(summarizerPrompt)
                .call()
                .content();

        // 解析客户端结果
        String summarizerJson = extractJson(summarizerResult);
        JSONObject summarizerObject = parseJsonObject(summarizerResult);
        if (summarizerObject == null) {
            summarizerObject = new JSONObject();
            summarizerObject.put(SUMMARIZER_OVERVIEW.getType(), summarizerJson);
        }
        parseSummarizerResult(executeDynamicContext, summarizerObject, executeRequestEntity.getSessionId());

        return router(executeRequestEntity, executeDynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

    private void parseSummarizerResult(ExecuteDynamicContext executeDynamicContext, JSONObject summarizerObject, String sessionId) {
        if (summarizerObject == null) {
            return;
        }
        sendSummarizerResult(executeDynamicContext, SUMMARIZER_OVERVIEW.getType(), summarizerObject.getString(SUMMARIZER_OVERVIEW.getType()), sessionId);
    }

    private void sendSummarizerResult(ExecuteDynamicContext executeDynamicContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
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
