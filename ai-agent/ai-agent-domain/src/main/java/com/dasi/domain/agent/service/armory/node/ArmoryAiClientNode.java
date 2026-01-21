package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.vo.AiClientVO;
import com.dasi.domain.agent.model.vo.AiPromptVO;
import com.dasi.domain.agent.service.armory.model.ArmoryContext;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dasi.domain.agent.model.enumeration.AiType.*;

@Slf4j
@Service
public class ArmoryAiClientNode extends AbstractArmoryNode {

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {

        Map<String, AiPromptVO> aiPromptVOMap = armoryContext.getValue(PROMPT.getType());
        List<AiClientVO> aiClientVOList = armoryContext.getValue(CLIENT.getType());

        if (aiClientVOList == null || aiClientVOList.isEmpty()) {
            log.warn("【装配节点】ArmoryAiClientNode：没有数据");
            return router(armoryRequestEntity, armoryContext);
        }

        for (AiClientVO aiClientVO : aiClientVOList) {

            // 1. 构建系统提示词
            StringBuilder system = new StringBuilder();
            List<String> promptIdList = aiClientVO.getPromptIdList();
            for (String promptId : promptIdList) {
                AiPromptVO aiPromptVO = aiPromptVOMap.get(promptId);
                system.append(aiPromptVO.getPromptContent());
            }

            // 2. 拿到 Model
            String modelId = aiClientVO.getModelId();
            String modelBeanName = MODEL.getBeanName(modelId);
            OpenAiChatModel openAiChatModel = getBean(modelBeanName);

            // 3. 拿到 Mcp
            List<McpSyncClient> mcpSyncClientList = new ArrayList<>();
            List<String> mcpIdList = aiClientVO.getMcpIdList();
            for (String mcpId : mcpIdList) {
                String mcpBeanName = MCP.getBeanName(mcpId);
                McpSyncClient mcpSyncClient = getBean(mcpBeanName);
                mcpSyncClientList.add(mcpSyncClient);
            }
            SyncMcpToolCallbackProvider toolCallbackList = new SyncMcpToolCallbackProvider(mcpSyncClientList.toArray(new McpSyncClient[0]));

            // 4. 拿到 Advisor
            List<Advisor> advisorList = new ArrayList<>();
            List<String> advisorIdList = aiClientVO.getAdvisorIdList();
            for (String advisorId : advisorIdList) {
                String advisorBeanName = ADVISOR.getBeanName(advisorId);
                Advisor advisor = getBean(advisorBeanName);
                advisorList.add(advisor);
            }

            // 5. 构建 Client
            ChatClient chatClient = ChatClient
                    .builder(openAiChatModel)
                    .defaultSystem(system.toString())
                    .defaultToolCallbacks(toolCallbackList)
                    .defaultAdvisors(advisorList)
                    .build();

            String clientBeanName = CLIENT.getBeanName(aiClientVO.getClientId());
            registerBean(clientBeanName, ChatClient.class, chatClient);
            log.info("【装配节点】ArmoryAiClientNode：clientBeanName={}", clientBeanName);
        }

        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return defaultStrategyHandler;
    }

}
