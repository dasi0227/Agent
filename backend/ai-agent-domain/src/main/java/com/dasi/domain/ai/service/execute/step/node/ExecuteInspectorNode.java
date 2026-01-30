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

import static com.dasi.domain.ai.model.enumeration.AiClientType.INSPECTOR;
import static com.dasi.domain.ai.model.enumeration.AiClientType.PLANNER;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.INSPECTOR_MCP;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_NONE;

@Slf4j
@Service(value = "inspectorNode")
public class ExecuteInspectorNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String inspectorJson;
        JSONArray inspectorArray;

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(INSPECTOR.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient inspectorClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();
            String inspectorPrompt = flowPrompt.formatted(
                    executeContext.getUserMessage()
            );

            // 获取客户端结果
            String inspectorResponse = inspectorClient
                    .prompt(inspectorPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE_NONE))
                    .call()
                    .content();

            // 解析客户端结果
            inspectorJson = extractJson(inspectorResponse, "[]");
            inspectorArray = parseJsonArray(inspectorJson);
            if (inspectorArray == null) {
                throw new IllegalStateException("Inspector 结果解析为空");
            }

        } catch (Exception e) {
            log.error("【执行节点】ExecuteInspectorNode：error={}", e.getMessage(), e);
            inspectorArray = buildExceptionArray(INSPECTOR.getExceptionType(), e.getMessage());
            inspectorJson = inspectorArray.toJSONString();
        }

        // 发送客户端结果
        parseInspectorResponse(executeContext, inspectorArray, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeContext.setValue(INSPECTOR.getContextKey(), inspectorJson);

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return getBean(PLANNER.getNodeName());
    }

    private void parseInspectorResponse(ExecuteContext executeContext, JSONArray inspectorArray, String sessionId) {
        if (inspectorArray == null) {
            return;
        }

        for (Object item : inspectorArray) {
            if (item instanceof JSONObject obj) {
                String sectionContent = obj.toJSONString();
                if (!sectionContent.isEmpty()) {
                    sendInspectorResponse(executeContext, INSPECTOR_MCP.getType(), sectionContent, sessionId);
                }
            }
        }
    }

    private void sendInspectorResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createInspectorResponse(
                    sectionType,
                    sectionContent,
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
