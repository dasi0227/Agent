package com.dasi.domain.agent.service.execute.step.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.entity.ExecuteResponseEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.AbstractExecuteNode;
import com.dasi.domain.agent.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.REPLIER;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.REPLIER_OVERVIEW;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "replierNode")
public class ExecuteReplierNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String replierJson;
        JSONObject replierObject;

        String executionHistory = executeContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory = "[暂无记录]";
        }

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(REPLIER.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient replierClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();
            String replierPrompt = flowPrompt.formatted(
                    executeContext.getUserMessage(),
                    executionHistory
            );

            // 获取客户端结果
            String replierResponse = replierClient
                    .prompt(replierPrompt)
                    .call()
                    .content();

            // 解析客户端结果
            replierJson = extractJson(replierResponse, "{}");
            replierObject = parseJsonObject(replierJson);
            if (replierObject == null) {
                throw new IllegalStateException("Replier 结果解析为空");
            }

        } catch (Exception e) {
            log.error("【执行节点】ExecuteReplierNode：error={}", e.getMessage(), e);
            replierObject = buildExceptionObject(REPLIER.getExceptionType(), e.getMessage());
            replierJson = replierObject.toJSONString();
        }

        log.info("\n=========================================== Replier ===========================================\n{}", replierJson);

        // 发送客户端结果
        parseReplierResponse(executeContext, replierObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeContext.setValue(REPLIER.getContextKey(), replierJson);

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return defaultStrategyHandler;
    }

    private void parseReplierResponse(ExecuteContext executeContext, JSONObject replierObject, String sessionId) {
        if (replierObject == null) {
            return;
        }
        sendReplierResponse(executeContext, REPLIER.getExceptionType(), replierObject.getString(REPLIER.getExceptionType()), sessionId);
        sendReplierResponse(executeContext, REPLIER_OVERVIEW.getType(), replierObject.getString(REPLIER_OVERVIEW.getType()), sessionId);
    }

    private void sendReplierResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createReplierResponse(
                    sectionType,
                    sectionContent,
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
