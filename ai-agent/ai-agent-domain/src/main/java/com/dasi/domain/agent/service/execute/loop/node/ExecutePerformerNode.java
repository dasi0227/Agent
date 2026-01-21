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

import static com.dasi.domain.agent.model.enumeration.AiClientType.PERFORMER;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.*;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecutePerformerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteLoopContext executeLoopContext) throws Exception {

        // 获取客户端
        AiFlowVO aiFlowVO = executeLoopContext.getAiFlowVOMap().get(PERFORMER.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient performerClient = getBean(clientBeanName);

        // 获取提示词
        String flowPrompt = aiFlowVO.getFlowPrompt();

        String analyzerResult = executeLoopContext.getValue("analyzerResult");
        if (analyzerResult == null || analyzerResult.trim().isEmpty()) {
            analyzerResult = "[任务分析专家异常，请你自行决定执行策略]";
        }

        String performerPrompt = String.format(flowPrompt,
                executeLoopContext.getOriginalTask(),
                analyzerResult
        );

        String performerJson;
        JSONObject performerObject;

        try {
            // 获取客户端结果
            String performerResult = performerClient
                    .prompt(performerPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 16384))
                    .call()
                    .content();

            // 解析客户端结果
            performerJson = extractJson(performerResult);
            performerObject = parseJsonObject(performerJson);
            if (performerObject == null) {
                throw new IllegalStateException("Performer 结果解析为空");
            }
        } catch (Exception e) {
            log.error("【执行节点】ExecutePerformerNode：error={}", e.getMessage(), e);
            performerObject = buildExceptionResult(PERFORMER.getExceptionType(), e.getMessage());
            performerJson = performerObject.toJSONString();
        }

        log.info("\n=========================================== Performer ===========================================\n{}", performerJson);
        parsePerformerResult(executeLoopContext, performerObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeLoopContext.setValue("performerResult", performerJson);

        return router(executeRequestEntity, executeLoopContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteLoopContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteLoopContext executeLoopContext) throws Exception {
        return getBean("executeSupervisorNode");
    }

    private void parsePerformerResult(ExecuteLoopContext executeLoopContext, JSONObject performerObject, String sessionId) {
        if (performerObject == null) {
            return;
        }
        sendPerformerResult(executeLoopContext, PERFORMER.getExceptionType(), performerObject.getString(PERFORMER.getExceptionType()), sessionId);
        sendPerformerResult(executeLoopContext, PERFORMER_TARGET.getType(), performerObject.getString(PERFORMER_TARGET.getType()), sessionId);
        sendPerformerResult(executeLoopContext, PERFORMER_PROCESS.getType(), performerObject.getString(PERFORMER_PROCESS.getType()), sessionId);
        sendPerformerResult(executeLoopContext, PERFORMER_RESULT.getType(), performerObject.getString(PERFORMER_RESULT.getType()), sessionId);
    }

    private void sendPerformerResult(ExecuteLoopContext executeLoopContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteLoopResult executeLoopResult = ExecuteLoopResult.createPerformerResult(
                    sectionType,
                    sectionContent,
                    executeLoopContext.getStep(),
                    sessionId
            );

            sendSseResult(executeLoopContext, executeLoopResult);
        }
    }

}
