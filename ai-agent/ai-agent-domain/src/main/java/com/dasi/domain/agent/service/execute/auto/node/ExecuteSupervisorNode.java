package com.dasi.domain.agent.service.execute.auto.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ExecuteCommandEntity;
import com.dasi.domain.agent.model.vo.AiFlowVO;
import com.dasi.domain.agent.service.execute.auto.factory.AutoExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.agent.model.enumeration.AiClientType.SUPERVISOR;
import static com.dasi.domain.agent.model.enumeration.AiType.CLIENT;

@Slf4j
@Service
public class ExecuteSupervisorNode extends AbstractExecuteNode {

    @Resource
    private ExecuteSummarizerNode executeSummarizerNode;

    @Resource
    private ExecuteAnalyzerNode executeAnalyzerNode;

    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        String analyzerResult = dynamicContext.getValue("analyzerResult");
        String performerResult = dynamicContext.getValue("performerResult");
        if (analyzerResult == null) {
            analyzerResult = "";
        }
        if (performerResult == null) {
            performerResult = "";
        }

        String supervisorPrompt = String.format("""
                你是 Supervisor 任务监督专家。
                请严格遵循 system prompt 的固定输出结构与字段名。
                你需要使用 MCP 工具进行联网搜索。
                参考信息：
                输入信息：
                - 用户原始需求：%s
                - 任务分析专家：%s
                - 任务执行专家：%s
                """,
                dynamicContext.getOriginalTask(),
                analyzerResult.isEmpty() ? "[任务分析师异常，输出为空，请你依据用户原始需求分析]" : analyzerResult,
                performerResult.isEmpty() ? "[任务执行师异常，输出为空，请你依据用户原始需求分析]" : performerResult
        );

        AiFlowVO aiFlowVO = dynamicContext.getAiFlowVOMap().get(SUPERVISOR.getCode());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient supervisorClient = getBean(clientBeanName);

        String supervisorResult = supervisorClient
                .prompt(supervisorPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommandEntity.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .call()
                .content();

        dynamicContext.setValue("supervisorResult", supervisorResult);

        if (supervisorResult.contains("FAIL")) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行不合格");
            dynamicContext.setCompleted(false);
            dynamicContext.setCurrentTask("根据任务监督专家的建议重新执行任务");
        } else if (supervisorResult.contains("OPTIMIZE")) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行有待优化");
            dynamicContext.setCompleted(false);
            dynamicContext.setCurrentTask("根据任务监督专家的建议优化执行任务");
        } else if (supervisorResult.contains("PASS")) {
            log.info("【执行节点】ExecuteSupervisorNode：任务执行合格");
            dynamicContext.setCompleted(true);
        }

        String executionHistory = String.format("""
                === 第 %d 步执行记录 ===
                【任务分析专家输出】%s
                【任务执行专家输出】%s
                【任务监督专家输出】%s
                """,
                dynamicContext.getStep(),
                analyzerResult,
                performerResult,
                supervisorResult
        );

        dynamicContext.getExecutionHistory().append(executionHistory);
        dynamicContext.setStep(dynamicContext.getStep() + 1);

        log.info("【执行节点】ExecuteSupervisorNode：{}", supervisorResult);
        return router(executeCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, AutoExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, AutoExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        ExecuteAnalyzerNode executeAnalyzerNode = getBean("executeAnalyzerNode");
        ExecuteSummarizerNode executeSummarizerNode = getBean("executeSummarizerNode");

        if (dynamicContext.getCompleted() == true) {
            log.info("【执行节点】ExecuteSupervisorNode：任务监督已完成");
            return executeSummarizerNode;
        } else if (dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            log.info("【执行节点】ExecuteSupervisorNode：任务已到达最大步数");
            return executeSummarizerNode;
        } else {
            return executeAnalyzerNode;
        }

    }

}
