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
import static com.dasi.domain.ai.model.enumeration.AiClientType.SUPERVISOR;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.*;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "performerNode")
public class ExecutePerformerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String performerJson;
        JSONObject performerObject;

        String analyzerResponse = executeContext.getValue(ANALYZER.getContextKey());
        if (analyzerResponse == null || analyzerResponse.trim().isEmpty()) {
            analyzerResponse = "[任务分析专家异常，请你自行决定执行策略]";
        }

        try {
            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(PERFORMER.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient performerClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();
            String performerPrompt = flowPrompt.formatted(
                    executeContext.getUserMessage(),
                    analyzerResponse
            );

            // 获取客户端结果
            String performerResponse = performerClient
                    .prompt(performerPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 16384))
                    .call()
                    .content();

            // 解析客户端结果
            performerJson = extractJson(performerResponse, "{}");
            performerObject = parseJsonObject(performerJson);
            if (performerObject == null) {
                throw new IllegalStateException("Performer 结果解析为空");
            }

        } catch (Exception e) {
            log.error("【执行节点】ExecutePerformerNode：error={}", e.getMessage(), e);
            performerObject = buildExceptionObject(PERFORMER.getExceptionType(), e.getMessage());
            performerJson = performerObject.toJSONString();
        }

        log.info("\n=========================================== Performer ===========================================\n{}", performerJson);

        // 发送客户端结果
        parsePerformerResponse(executeContext, performerObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeContext.setValue(PERFORMER.getContextKey(), performerJson);

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return getBean(SUPERVISOR.getNodeName());
    }

    private void parsePerformerResponse(ExecuteContext executeContext, JSONObject performerObject, String sessionId) {
        if (performerObject == null) {
            return;
        }
        sendPerformerResponse(executeContext, PERFORMER.getExceptionType(), performerObject.getString(PERFORMER.getExceptionType()), sessionId);
        sendPerformerResponse(executeContext, PERFORMER_TARGET.getType(), performerObject.getString(PERFORMER_TARGET.getType()), sessionId);
        sendPerformerResponse(executeContext, PERFORMER_PROCESS.getType(), performerObject.getString(PERFORMER_PROCESS.getType()), sessionId);
        sendPerformerResponse(executeContext, PERFORMER_RESULT.getType(), performerObject.getString(PERFORMER_RESULT.getType()), sessionId);
    }

    private void sendPerformerResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createPerformerResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getRound(),
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
