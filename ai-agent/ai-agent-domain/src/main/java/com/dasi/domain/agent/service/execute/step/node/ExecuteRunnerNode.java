package com.dasi.domain.agent.service.execute.step.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.entity.ExecuteResponseEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.AbstractExecuteNode;
import com.dasi.domain.agent.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiClientType.*;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.RUNNER_RESULT;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.RUNNER_STATUS;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "runnerNode")
public class ExecuteRunnerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String runnerJson;
        JSONObject runnerObject;

        String inspectorResponse = executeContext.getValue(INSPECTOR.getContextKey());
        if (inspectorResponse == null || inspectorResponse.isEmpty()) {
            inspectorResponse = "[任务审查专家异常，请你依据用户原始需求分析]";
        }

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(RUNNER.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient runnerClient = getBean(clientBeanName);

            List<String> plannerList = executeContext.getValue(PLANNER.getContextKey());
            if (plannerList == null || plannerList.isEmpty()) {
                log.error("【执行节点】ExecuteRunnerNode：执行步骤为空，错误");
                return router(executeRequestEntity, executeContext);
            }

            List<String> runnerList = new ArrayList<>();
            int maxRetry = executeContext.getMaxRetry();

            for (int step = 0; step < plannerList.size(); ) {

                int count = 1;

                while (true) {

                    try {
                        // 获取提示词
                        String flowPrompt = aiFlowVO.getFlowPrompt();
                        String taskContent = plannerList.get(step);

                        String runnerPrompt = flowPrompt.formatted(
                                executeContext.getUserMessage(),
                                inspectorResponse,
                                taskContent
                        );

                        // 获取客户端结果
                        String runnerResponse = runnerClient
                                .prompt(runnerPrompt)
                                .advisors(a -> a
                                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 16384))
                                .call()
                                .content();

                        // 解析客户端结果
                        runnerJson = extractJson(runnerResponse, "{}");
                        runnerObject = parseJsonObject(runnerJson);

                        String status = runnerObject.getString(RUNNER_STATUS.getType());

                        if ("FAIL".equalsIgnoreCase(status)) {
                            String runnerResult = runnerObject.get(RUNNER_RESULT.getType()).toString();
                            String failReason = StringUtils.isBlank(runnerResult) ? "客户端执行失败" : runnerResult;
                            throw new IllegalStateException(failReason);
                        }

                        break;

                    } catch (Exception e) {
                        log.error("【执行节点】ExecuteRunnerNode：step={}, try={}/{}, error={}", step + 1, count++, maxRetry, e.getMessage(), e);
                        if (count >= maxRetry) {
                            throw new IllegalStateException("超过最大重试次数 " + maxRetry + "，客户端仍然无法执行步骤 " + (step + 1));
                        }
                    }
                }

                log.info("\n=========================================== Runner ===========================================\n{}", runnerJson);

                // 发送客户端结果
                runnerList.add(runnerJson);
                executeContext.setStep(++step);
                parseRunnerResponse(executeContext, runnerObject, executeRequestEntity.getSessionId());
            }

            // 保存客户端结果
            executeContext.setValue(RUNNER.getContextKey(), runnerList);

            String plannerResponse = String.join("\n", plannerList);
            String runnerResponse = String.join("\n", runnerList);
            String executionHistory = String.format("""
                        === 执行记录 ===
                        【任务审查专家】
                        %s
                        【任务规划专家】
                        %s
                        【任务运行专家】
                        %s
                        """,
                    inspectorResponse,
                    plannerResponse,
                    runnerResponse
            );

            executeContext.getExecutionHistory().append(executionHistory);

        } catch (Exception e) {
            log.error("【执行节点】ExecuteRunnerNode：error={}", e.getMessage(), e);
            runnerObject = buildExceptionObject(RUNNER.getExceptionType(), e.getMessage());
            parseRunnerResponse(executeContext, runnerObject, executeRequestEntity.getSessionId());
        }

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return getBean(REPLIER.getNodeName());
    }

    private void parseRunnerResponse(ExecuteContext executeContext, JSONObject runnerObject, String sessionId) {
        if (runnerObject == null) {
            return;
        }
        sendRunnerResponse(executeContext, RUNNER.getExceptionType(), runnerObject.getString(RUNNER.getExceptionType()), sessionId);
        sendRunnerResponse(executeContext, RUNNER_RESULT.getType(), runnerObject.getString(RUNNER_RESULT.getType()), sessionId);
        sendRunnerResponse(executeContext, RUNNER_STATUS.getType(), runnerObject.getString(RUNNER_STATUS.getType()), sessionId);
    }

    private void sendRunnerResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createRunnerResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getStep(),
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
