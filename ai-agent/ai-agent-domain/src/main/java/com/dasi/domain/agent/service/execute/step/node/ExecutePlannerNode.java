package com.dasi.domain.agent.service.execute.step.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONArray;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.AbstractExecuteNode;
import com.dasi.domain.agent.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.INSPECTOR;
import static com.dasi.domain.agent.model.enumeration.AiClientType.PLANNER;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "plannerNode")
public class ExecutePlannerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String plannerJson;
        JSONArray plannerArray;

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(PLANNER.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient plannerClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();
            String inspectorPrompt = String.format(flowPrompt,
                    executeContext.getUserMessage()
            );

            // 获取客户端结果
            String inspectorResult = plannerClient
                    .prompt(inspectorPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 0))
                    .call()
                    .content();

            // 解析客户端结果
            plannerJson = extractJson(inspectorResult, "[]");
            plannerArray = parseJsonArray(plannerJson);
            if (plannerArray == null) {
                throw new IllegalStateException("Inspector 结果解析为空");
            }

        } catch (Exception e) {
            log.error("【执行节点】ExecuteInspectorNode：error={}", e.getMessage(), e);
            plannerArray = buildExceptionArray(INSPECTOR.getExceptionType(), e.getMessage());
            plannerJson = plannerArray.toJSONString();
        }

        log.info("\n=========================================== Inspector ===========================================\n{}", plannerJson);

        // 发送客户端结果
        parseInspectorResult(executeContext, plannerArray, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeContext.setValue(PLANNER.getContextKey(), plannerJson);

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return null;
    }

}
