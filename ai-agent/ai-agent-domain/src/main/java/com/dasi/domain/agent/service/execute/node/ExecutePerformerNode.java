package com.dasi.domain.agent.service.execute.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
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

        String analyzerResult = executeDynamicContext.getValue("analyzerResult");
        if (analyzerResult == null || analyzerResult.trim().isEmpty()) {
            analyzerResult = "[任务分析专家异常，请你自行决定执行策略]";
        }

        String performerPrompt = String.format("""
                你是一名专业的 Performer 任务执行专家。
                
                你需要基于提供的信息，根据用户需求和任务分析专家的输出，实际执行具体的任务。
                
                执行要求：
                1. 直接执行用户的具体需求（如搜索、检索、生成内容等）
                2. 如果需要搜索网络信息，请实际进行搜索和检索
                3. 如果需要生成计划、列表等，请直接生成完整内容
                4. 提供具体的执行结果，而不只是描述过程
                5. 确保执行结果能直接回答用户的问题
                
                参考信息：
                【用户原始需求】%s
                【任务分析专家】
                %s
                
                输出格式要求（必须严格遵守）：
                执行目标：[明确的执行目标]
                执行过程：[实际执行的步骤和调用的工具]
                执行结果：[执行成功和生成的内容]
                """,
                executeDynamicContext.getOriginalTask(),
                analyzerResult
        );

        // 获取客户端
        AiFlowVO aiFlowVO = executeDynamicContext.getAiFlowVOMap().get(PERFORMER.getType());
        String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
        ChatClient performerClient = getBean(clientBeanName);

        // 获取客户端结果
        String performerResult = performerClient
                .prompt(performerPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .call()
                .content();

        // 解析客户端结果
        parsePerformerResult(executeDynamicContext, performerResult, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeDynamicContext.setValue("performerResult", performerResult);

        return router(executeRequestEntity, executeDynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteDynamicContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) throws Exception {
        return getBean("executeSupervisorNode");
    }

    private void parsePerformerResult(ExecuteDynamicContext executeDynamicContext, String performerResult, String sessionId) {
        String[] lines = performerResult.split("\n");
        String sectionType = "";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 每次都发送上一次积累的 section
            if (line.contains(PERFORMER_TARGET.getName())) {
                log.info("【执行节点】ExecutePerformerNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendPerformerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = PERFORMER_TARGET.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(PERFORMER_PROCESS.getName())) {
                log.info("【执行节点】ExecutePerformerNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendPerformerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = PERFORMER_PROCESS.getType();
                sectionContent = new StringBuilder();
            } else if (line.contains(PERFORMER_RESULT.getName())) {
                log.info("【执行节点】ExecutePerformerNode：sectionType={}, sectionContent={}", sectionType, sectionContent);
                sendPerformerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
                sectionType = PERFORMER_RESULT.getType();
                sectionContent = new StringBuilder();
            } else {
                sectionContent.append(line).append("\n");
            }
        }

        // 发送最后的 section
        sendPerformerResult(executeDynamicContext, sectionType, sectionContent.toString(), sessionId);
    }

    private void sendPerformerResult(ExecuteDynamicContext executeDynamicContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && !sectionContent.isEmpty()) {
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
