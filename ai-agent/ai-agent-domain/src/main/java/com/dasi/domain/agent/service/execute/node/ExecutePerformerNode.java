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

import static com.dasi.domain.agent.model.enumeration.AiClientType.PERFORMER;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.*;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecutePerformerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {

        // 获取客户端
        AiFlowVO aiFlowVO = executeDynamicContext.getAiFlowVOMap().get(PERFORMER.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient performerClient = getBean(clientBeanName);

        // 获取提示词
        String flowPrompt = aiFlowVO.getFlowPrompt();

        String analyzerResult = executeDynamicContext.getValue("analyzerResult");
        if (analyzerResult == null || analyzerResult.trim().isEmpty()) {
            analyzerResult = "[任务分析专家异常，请你自行决定执行策略]";
        }

        String performerPrompt = String.format(flowPrompt,
                executeDynamicContext.getOriginalTask(),
                analyzerResult
        );

        // 获取客户端结果
        String performerResult = performerClient
                .prompt(performerPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 16384))
                .call()
                .content();

        // 解析客户端结果
        String performerJson = extractJson(performerResult);
        JSONObject performerObject = parseJsonObject(performerJson);
        log.info("\n=========================================== Performer ===========================================\n{}", performerJson);
        if (performerObject == null) {
            performerObject = new JSONObject();
            performerObject.put(PERFORMER_RESULT.getType(), performerJson);
        }
        parsePerformerResult(executeDynamicContext, performerObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeDynamicContext.setValue("performerResult", performerJson);

        return router(executeRequestEntity, executeDynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {
        return getBean("executeSupervisorNode");
    }

    private void parsePerformerResult(ExecuteDynamicContext executeDynamicContext, JSONObject performerObject, String sessionId) {
        if (performerObject == null) {
            return;
        }
        sendPerformerResult(executeDynamicContext, PERFORMER_TARGET.getType(), performerObject.getString(PERFORMER_TARGET.getType()), sessionId);
        sendPerformerResult(executeDynamicContext, PERFORMER_PROCESS.getType(), performerObject.getString(PERFORMER_PROCESS.getType()), sessionId);
        sendPerformerResult(executeDynamicContext, PERFORMER_RESULT.getType(), performerObject.getString(PERFORMER_RESULT.getType()), sessionId);
    }

    private void sendPerformerResult(ExecuteDynamicContext executeDynamicContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteAutoResultEntity executeAutoResultEntity = ExecuteAutoResultEntity.createPerformerResult(
                    sectionType,
                    sectionContent,
                    executeDynamicContext.getStep(),
                    sessionId
            );

            sendSseResult(executeDynamicContext, executeAutoResultEntity);
        }
    }

}
