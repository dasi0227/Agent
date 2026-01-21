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

        String executionHistory = executeDynamicContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory =  "[暂无记录]";
        }

        String summarizerPrompt = String.format(flowPrompt,
                executeDynamicContext.getOriginalTask(),
                executionHistory
        );

        String summarizerJson;
        JSONObject summarizerObject;

        try {
            // 获取客户端结果
            String summarizerResult = summarizerClient
                    .prompt(summarizerPrompt)
                    .call()
                    .content();

            // 解析客户端结果
            summarizerJson = extractJson(summarizerResult);
            summarizerObject = parseJsonObject(summarizerJson);
            if (summarizerObject == null) {
                throw new IllegalStateException("Summarizer 结果解析为空");
            }
        } catch (Exception e) {
            log.error("【执行节点】ExecuteSummarizerNode：error={}", e.getMessage(), e);
            summarizerObject = buildExceptionResult(SUMMARIZER.getExceptionType(), e.getMessage());
            summarizerJson = summarizerObject.toJSONString();
        }

        log.info("\n=========================================== Summarizer ===========================================\n{}", summarizerJson);
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
        sendSummarizerResult(executeDynamicContext, SUMMARIZER.getExceptionType(), summarizerObject.getString(SUMMARIZER.getExceptionType()), sessionId);
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
