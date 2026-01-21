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

import static com.dasi.domain.agent.model.enumeration.AiClientType.SUPERVISOR;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.*;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecuteSupervisorNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteLoopContext executeLoopContext) throws Exception {

        // 获取客户端
        AiFlowVO aiFlowVO = executeLoopContext.getAiFlowVOMap().get(SUPERVISOR.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient supervisorClient = getBean(clientBeanName);

        // 获取提示词
        String flowPrompt = aiFlowVO.getFlowPrompt();

        String analyzerResult = executeLoopContext.getValue("analyzerResult");
        String performerResult = executeLoopContext.getValue("performerResult");

        if (analyzerResult == null || analyzerResult.trim().isEmpty()) {
            analyzerResult = "[任务分析专家异常，请你依据用户原始需求分析]";
        }
        if (performerResult == null || performerResult.trim().isEmpty()) {
            performerResult = "[任务执行专家异常，请你依据用户原始需求分析]";
        }

        String supervisorPrompt = String.format(flowPrompt,
                executeLoopContext.getOriginalTask(),
                analyzerResult,
                performerResult
        );

        String supervisorJson;
        JSONObject supervisorObject;

        try {
            // 获取客户端结果
            String supervisorResult = supervisorClient
                    .prompt(supervisorPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 4096))
                    .call()
                    .content();

            // 解析客户端结果
            supervisorJson = extractJson(supervisorResult);
            supervisorObject = parseJsonObject(supervisorJson);
            if (supervisorObject == null) {
                throw new IllegalStateException("Supervisor 结果解析为空");
            }
        } catch (Exception e) {
            log.error("【执行节点】ExecuteSupervisorNode：error={}", e.getMessage(), e);
            supervisorObject = buildExceptionResult(SUPERVISOR.getExceptionType(), e.getMessage());
            supervisorJson = supervisorObject.toJSONString();
        }

        log.info("\n=========================================== Supervisor ===========================================\n{}", supervisorJson);
        parseSupervisorResult(executeLoopContext, supervisorObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeLoopContext.setValue("supervisorResult", supervisorJson);

        // 检查客户端结果
        String supervisorStatus = supervisorObject.getString(SUPERVISOR_STATUS.getType());
        if ("FAIL".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行不合格");
            executeLoopContext.setCompleted(false);
            executeLoopContext.setCurrentTask("根据任务监督专家的输出重新执行任务");
        } else if ("OPTIMIZE".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行有待优化");
            executeLoopContext.setCompleted(false);
            executeLoopContext.setCurrentTask("根据任务监督专家的输出优化执行任务");
        } else if ("PASS".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行合格");
            executeLoopContext.setCompleted(true);
        }

        // 更新客户端历史
        String executionHistory = String.format("""
                === 第 %d 步执行记录 ===
                【任务分析专家】
                %s
                【任务执行专家】
                %s
                【任务监督专家】
                %s
                """,
                executeLoopContext.getStep(),
                analyzerResult,
                performerResult,
                supervisorJson
        );

        executeLoopContext.getExecutionHistory().append(executionHistory);
        executeLoopContext.setStep(executeLoopContext.getStep() + 1);

        return router(executeRequestEntity, executeLoopContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteLoopContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteLoopContext executeLoopContext) throws Exception {

        ExecuteAnalyzerNode executeAnalyzerNode = getBean("executeAnalyzerNode");
        ExecuteSummarizerNode executeSummarizerNode = getBean("executeSummarizerNode");

        if (executeLoopContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteSupervisorNode：任务监督达标");
            return executeSummarizerNode;
        } else if (executeLoopContext.getStep() > executeLoopContext.getMaxStep()) {
            log.info("【执行节点】ExecuteSupervisorNode：任务已到达最大步数");
            return executeSummarizerNode;
        } else {
            return executeAnalyzerNode;
        }
    }

    private void parseSupervisorResult(ExecuteLoopContext executeLoopContext, JSONObject supervisorObject, String sessionId) {
        if (supervisorObject == null) {
            return;
        }
        sendSupervisorResult(executeLoopContext, SUPERVISOR.getExceptionType(), supervisorObject.getString(SUPERVISOR.getExceptionType()), sessionId);
        sendSupervisorResult(executeLoopContext, SUPERVISOR_ISSUE.getType(), supervisorObject.getString(SUPERVISOR_ISSUE.getType()), sessionId);
        sendSupervisorResult(executeLoopContext, SUPERVISOR_SUGGESTION.getType(), supervisorObject.getString(SUPERVISOR_SUGGESTION.getType()), sessionId);
        sendSupervisorResult(executeLoopContext, SUPERVISOR_SCORE.getType(), supervisorObject.getString(SUPERVISOR_SCORE.getType()), sessionId);
        sendSupervisorResult(executeLoopContext, SUPERVISOR_STATUS.getType(), supervisorObject.getString(SUPERVISOR_STATUS.getType()), sessionId);
    }

    private void sendSupervisorResult(ExecuteLoopContext executeLoopContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteLoopResult executeLoopResult = ExecuteLoopResult.createSupervisorResult(
                    sectionType,
                    sectionContent,
                    executeLoopContext.getStep(),
                    sessionId
            );

            sendSseResult(executeLoopContext, executeLoopResult);
        }
    }

}
