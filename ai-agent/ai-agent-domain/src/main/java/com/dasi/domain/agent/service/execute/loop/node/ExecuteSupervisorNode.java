package com.dasi.domain.agent.service.execute.loop.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.agent.model.entity.ExecuteResponseEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.AbstractExecuteNode;
import com.dasi.domain.agent.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.SUPERVISOR;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.*;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "executeSupervisorNode")
public class ExecuteSupervisorNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String supervisorJson;
        JSONObject supervisorObject;

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(SUPERVISOR.getType());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient supervisorClient = getBean(clientBeanName);

            // 获取提示词
            String flowPrompt = aiFlowVO.getFlowPrompt();

            String analyzerResult = executeContext.getValue("analyzerResult");
            String performerResult = executeContext.getValue("performerResult");

            if (analyzerResult == null || analyzerResult.trim().isEmpty()) {
                analyzerResult = "[任务分析专家异常，请你依据用户原始需求分析]";
            }
            if (performerResult == null || performerResult.trim().isEmpty()) {
                performerResult = "[任务执行专家异常，请你依据用户原始需求分析]";
            }

            String supervisorPrompt = String.format(flowPrompt,
                    executeContext.getUserMessage(),
                    analyzerResult,
                    performerResult
            );


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

            // 更新客户端历史
            String executionHistory = String.format("""
                        === 第 %d 轮执行记录 ===
                        【任务分析专家】
                        %s
                        【任务执行专家】
                        %s
                        【任务监督专家】
                        %s
                        """,
                    executeContext.getRound(),
                    analyzerResult,
                    performerResult,
                    supervisorJson
            );

            executeContext.getExecutionHistory().append(executionHistory);
            executeContext.setRound(executeContext.getRound() + 1);

        } catch (Exception e) {
            log.error("【执行节点】ExecuteSupervisorNode：error={}", e.getMessage(), e);
            supervisorObject = buildExceptionResult(SUPERVISOR.getExceptionType(), e.getMessage());
            supervisorJson = supervisorObject.toJSONString();
        }

        log.info("\n=========================================== Supervisor ===========================================\n{}", supervisorJson);
        parseSupervisorResult(executeContext, supervisorObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeContext.setValue("supervisorResult", supervisorJson);

        // 检查客户端结果
        String supervisorStatus = supervisorObject.getString(SUPERVISOR_STATUS.getType());
        if ("FAIL".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行不合格");
            executeContext.setCompleted(false);
            executeContext.setCurrentTask("根据任务监督专家的输出重新执行任务");
        } else if ("OPTIMIZE".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行有待优化");
            executeContext.setCompleted(false);
            executeContext.setCurrentTask("根据任务监督专家的输出优化执行任务");
        } else if ("PASS".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行合格");
            executeContext.setCompleted(true);
        }

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        ExecuteAnalyzerNode executeAnalyzerNode = getBean("executeAnalyzerNode");
        ExecuteSummarizerNode executeSummarizerNode = getBean("executeSummarizerNode");

        if (executeContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteSupervisorNode：任务监督达标");
            return executeSummarizerNode;
        } else if (executeContext.getRound() > executeContext.getMaxRound()) {
            log.info("【执行节点】ExecuteSupervisorNode：任务已到达最大轮数");
            return executeSummarizerNode;
        } else {
            return executeAnalyzerNode;
        }
    }

    private void parseSupervisorResult(ExecuteContext executeContext, JSONObject supervisorObject, String sessionId) {
        if (supervisorObject == null) {
            return;
        }
        sendSupervisorResult(executeContext, SUPERVISOR.getExceptionType(), supervisorObject.getString(SUPERVISOR.getExceptionType()), sessionId);
        sendSupervisorResult(executeContext, SUPERVISOR_ISSUE.getType(), supervisorObject.getString(SUPERVISOR_ISSUE.getType()), sessionId);
        sendSupervisorResult(executeContext, SUPERVISOR_SUGGESTION.getType(), supervisorObject.getString(SUPERVISOR_SUGGESTION.getType()), sessionId);
        sendSupervisorResult(executeContext, SUPERVISOR_SCORE.getType(), supervisorObject.getString(SUPERVISOR_SCORE.getType()), sessionId);
        sendSupervisorResult(executeContext, SUPERVISOR_STATUS.getType(), supervisorObject.getString(SUPERVISOR_STATUS.getType()), sessionId);
    }

    private void sendSupervisorResult(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createSupervisorResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getRound(),
                    sessionId
            );

            sendSseResult(executeContext, executeResponseEntity);
        }
    }

}
