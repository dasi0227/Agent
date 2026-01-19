package com.dasi.domain.agent.service.execute.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
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
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .call()
                .content();

        // 保存客户端结果
        executeDynamicContext.setValue("supervisorResult", supervisorResult);

        // 解析客户端结果
        parseSupervisorResult(executeDynamicContext, supervisorResult, executeRequestEntity.getSessionId());

        // 检查客户端结果
        if (supervisorResult.contains("FAIL")) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行不合格");
            executeDynamicContext.setCompleted(false);
            executeDynamicContext.setCurrentTask("根据任务监督专家的输出重新执行任务");
        } else if (supervisorResult.contains("OPTIMIZE")) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行有待优化");
            executeDynamicContext.setCompleted(false);
            executeDynamicContext.setCurrentTask("根据任务监督专家的输出优化执行任务");
        } else if (supervisorResult.contains("PASS")) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行合格");
            executeDynamicContext.setCompleted(true);
        }

        // 更新客户端历史
        String executionHistory = String.format("""
                === 第 %d 步执行记录 ===
                【任务分析专家输出】%s
                【任务执行专家输出】%s
                【任务监督专家输出】%s
                """,
                executeDynamicContext.getStep(),
                analyzerResult,
                performerResult,
                supervisorResult
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
            log.info("【执行节点】ExecuteSupervisorNode：任务监督已完成");
            return executeSummarizerNode;
        } else if (executeDynamicContext.getStep() > executeDynamicContext.getMaxStep()) {
            log.info("【执行节点】ExecuteSupervisorNode：任务已到达最大步数");
            return executeSummarizerNode;
        } else {
            return executeAnalyzerNode;
        }
    }

    private void parseSupervisorResult(ExecuteDynamicContext executeDynamicContext, String supervisorResult, String sessionId) {

        String[] lines = supervisorResult.split("\n");
        String sectionType = "";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 每次都发送上一次积累的 section
            if (line.contains(SUPERVISOR_ISSUE.getName())) {
                log.info("【执行节点】ExecuteSupervisorNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendSupervisorResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = SUPERVISOR_ISSUE.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(SUPERVISOR_SUGGESTION.getName())) {
                log.info("【执行节点】ExecuteSupervisorNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendSupervisorResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = SUPERVISOR_SUGGESTION.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(SUPERVISOR_SCORE.getName())) {
                log.info("【执行节点】ExecuteSupervisorNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendSupervisorResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = SUPERVISOR_SCORE.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(SUPERVISOR_MATCH.getName())) {
                log.info("【执行节点】ExecuteSupervisorNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendSupervisorResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = SUPERVISOR_MATCH.getType();
                sectionContent = new StringBuilder();
            }else if (line.contains(SUPERVISOR_STATUS.getName())) {
                log.info("【执行节点】ExecuteSupervisorNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendSupervisorResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = SUPERVISOR_STATUS.getType();
                sectionContent = new StringBuilder();
            } else {
                sectionContent.append(line).append("\n");
            }
        }

        // 发送最后的 section
        sendSupervisorResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
    }

    private void sendSupervisorResult(ExecuteDynamicContext executeDynamicContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && !sectionContent.isEmpty()) {
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
