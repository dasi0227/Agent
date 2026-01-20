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

import static com.dasi.domain.agent.model.enumeration.AiClientType.SUPERVISOR;
import static com.dasi.domain.agent.model.enumeration.AiSectionType.*;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecuteSupervisorNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {

        // 获取客户端
        AiFlowVO aiFlowVO = executeDynamicContext.getAiFlowVOMap().get(SUPERVISOR.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient supervisorClient = getBean(clientBeanName);

        // 获取提示词
        String flowPrompt = aiFlowVO.getFlowPrompt();

        String analyzerResult = executeDynamicContext.getValue("analyzerResult");
        String performerResult = executeDynamicContext.getValue("performerResult");

        if (analyzerResult == null || analyzerResult.trim().isEmpty()) {
            analyzerResult = "[任务分析专家异常，请你依据用户原始需求分析]";
        }
        if (performerResult == null || performerResult.trim().isEmpty()) {
            performerResult = "[任务执行专家异常，请你依据用户原始需求分析]";
        }

        String supervisorPrompt = String.format(flowPrompt,
                executeDynamicContext.getOriginalTask(),
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
        String supervisorJson = extractJson(supervisorResult);
        JSONObject supervisorObject = parseJsonObject(supervisorJson);
        log.info("\n=========================================== Supervisor ===========================================\n{}", supervisorJson);
        if (supervisorObject == null) {
            supervisorObject = new JSONObject();
            supervisorObject.put(SUPERVISOR_ISSUE.getType(), supervisorJson);
        }
        parseSupervisorResult(executeDynamicContext, supervisorObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeDynamicContext.setValue("supervisorResult", supervisorJson);

        // 检查客户端结果
        String supervisorStatus = supervisorObject.getString(SUPERVISOR_STATUS.getType());
        if ("FAIL".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行不合格");
            executeDynamicContext.setCompleted(false);
            executeDynamicContext.setCurrentTask("根据任务监督专家的输出重新执行任务");
        } else if ("OPTIMIZE".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行有待优化");
            executeDynamicContext.setCompleted(false);
            executeDynamicContext.setCurrentTask("根据任务监督专家的输出优化执行任务");
        } else if ("PASS".equalsIgnoreCase(supervisorStatus)) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行合格");
            executeDynamicContext.setCompleted(true);
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
                executeDynamicContext.getStep(),
                analyzerResult,
                performerResult,
                supervisorJson
        );

        executeDynamicContext.getExecutionHistory().append(executionHistory);
        executeDynamicContext.setStep(executeDynamicContext.getStep() + 1);

        return router(executeRequestEntity, executeDynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {

        ExecuteAnalyzerNode executeAnalyzerNode = getBean("executeAnalyzerNode");
        ExecuteSummarizerNode executeSummarizerNode = getBean("executeSummarizerNode");

        if (executeDynamicContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteSupervisorNode：任务监督达标");
            return executeSummarizerNode;
        } else if (executeDynamicContext.getStep() > executeDynamicContext.getMaxStep()) {
            log.info("【执行节点】ExecuteSupervisorNode：任务已到达最大步数");
            return executeSummarizerNode;
        } else {
            return executeAnalyzerNode;
        }
    }

    private void parseSupervisorResult(ExecuteDynamicContext executeDynamicContext, JSONObject supervisorObject, String sessionId) {
        if (supervisorObject == null) {
            return;
        }
        sendSupervisorResult(executeDynamicContext, SUPERVISOR_ISSUE.getType(), supervisorObject.getString(SUPERVISOR_ISSUE.getType()), sessionId);
        sendSupervisorResult(executeDynamicContext, SUPERVISOR_SUGGESTION.getType(), supervisorObject.getString(SUPERVISOR_SUGGESTION.getType()), sessionId);
        sendSupervisorResult(executeDynamicContext, SUPERVISOR_SCORE.getType(), supervisorObject.getString(SUPERVISOR_SCORE.getType()), sessionId);
        sendSupervisorResult(executeDynamicContext, SUPERVISOR_STATUS.getType(), supervisorObject.getString(SUPERVISOR_STATUS.getType()), sessionId);
    }

    private void sendSupervisorResult(ExecuteDynamicContext executeDynamicContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteAutoResultEntity executeAutoResultEntity = ExecuteAutoResultEntity.createSupervisorResult(
                    sectionType,
                    sectionContent,
                    executeDynamicContext.getStep(),
                    sessionId
            );

            sendSseResult(executeDynamicContext, executeAutoResultEntity);
        }
    }

}
