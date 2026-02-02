package com.dasi.domain.ai.service.execute.step.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.entity.ExecuteResponseEntity;
import com.dasi.domain.ai.model.vo.AiFlowVO;
import com.dasi.domain.ai.service.execute.AbstractExecuteNode;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.domain.ai.model.enumeration.AiRoleType.*;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.PLANNER_STEP;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_WORK;

@Slf4j
@Service(value = "plannerNode")
public class ExecutePlannerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String plannerJson;
        JSONArray plannerArray;

        String inspectorResponse = executeContext.getValue(INSPECTOR.getContextKey());
        if (inspectorResponse == null || inspectorResponse.isEmpty()) {
            inspectorResponse = "[任务审查专家异常，请你依据用户原始需求分析]";
        }

        try {
            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(PLANNER.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient plannerClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();
            String plannerPrompt = flowPrompt.formatted(
                    executeContext.getUserMessage(),
                    inspectorResponse
            );

            // 获取客户端结果
            String plannerResponse = plannerClient
                    .prompt(plannerPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE_WORK))
                    .call()
                    .content();

            // 解析客户端结果
            plannerJson = extractJson(plannerResponse, "[]");
            plannerArray = parseJsonArray(plannerJson);
            if (plannerArray == null) {
                throw new IllegalStateException("Planner 结果解析为空");
            }

        } catch (Exception e) {
            log.error("【执行节点】ExecutePlannerNode：error={}", e.getMessage(), e);
            plannerArray = buildExceptionArray(PLANNER.getExceptionType(), e.getMessage());
            plannerJson = plannerArray.toJSONString();
        }

        // 发送客户端结果
        parsePlannerResponse(executeContext, plannerArray, executeRequestEntity.getSessionId());

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return getBean(RUNNER.getNodeName());
    }

    private void parsePlannerResponse(ExecuteContext executeContext, JSONArray plannerArray, String sessionId) {
        if (plannerArray == null) {
            return;
        }

        List<String> plannerList = new ArrayList<>();

        for (Object item : plannerArray) {
            if (item instanceof JSONObject obj) {
                String sectionContent = obj.toJSONString();
                if (!sectionContent.isEmpty()) {
                    sendPlannerResponse(executeContext, PLANNER_STEP.getType(), sectionContent, sessionId);
                    plannerList.add(sectionContent);
                }
            }
        }

        // 保存客户端结果
        executeContext.setValue(PLANNER.getContextKey(), plannerList);
    }

    private void sendPlannerResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createPlannerResponse(
                    sectionType,
                    sectionContent,
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
