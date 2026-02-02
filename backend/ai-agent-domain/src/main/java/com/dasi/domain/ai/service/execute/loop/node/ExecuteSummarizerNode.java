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

import static com.dasi.domain.ai.model.enumeration.AiRoleType.SUMMARIZER;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.SUMMARIZER_OVERVIEW;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "summarizerNode")
public class ExecuteSummarizerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String summarizerJson;
        JSONObject summarizerObject;

        String executionHistory = executeContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory = "[暂无记录]";
        }

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(SUMMARIZER.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient summarizerClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();
            String summarizerPrompt = flowPrompt.formatted(
                    executeContext.getUserMessage(),
                    executionHistory
            );

            // 获取客户端结果
            String summarizerResponse = summarizerClient
                    .prompt(summarizerPrompt)
                    .call()
                    .content();

            // 解析客户端结果
            summarizerJson = extractJson(summarizerResponse, "{}");
            summarizerObject = parseJsonObject(summarizerJson);
            if (summarizerObject == null) {
                throw new IllegalStateException("Summarizer 结果解析为空");
            }

        } catch (Exception e) {
            log.error("【执行节点】ExecuteSummarizerNode：error={}", e.getMessage(), e);
            summarizerObject = buildExceptionObject(SUMMARIZER.getExceptionType(), e.getMessage());
            summarizerJson = summarizerObject.toJSONString();
        }

        // 发送客户端结果
        parseSummarizerResponse(executeContext, summarizerObject, executeRequestEntity.getSessionId());

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return defaultStrategyHandler;
    }

    private void parseSummarizerResponse(ExecuteContext executeContext, JSONObject summarizerObject, String sessionId) {
        if (summarizerObject == null) {
            return;
        }
        sendSummarizerResponse(executeContext, SUMMARIZER.getExceptionType(), summarizerObject.getString(SUMMARIZER.getExceptionType()), sessionId);
        sendSummarizerResponse(executeContext, SUMMARIZER_OVERVIEW.getType(), summarizerObject.getString(SUMMARIZER_OVERVIEW.getType()), sessionId);
    }

    private void sendSummarizerResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createSummarizerResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getRound(),
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
