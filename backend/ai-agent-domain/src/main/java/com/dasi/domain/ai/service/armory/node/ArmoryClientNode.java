package com.dasi.domain.ai.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.vo.AiClientVO;
import com.dasi.domain.ai.model.vo.AiPromptVO;
import com.dasi.domain.ai.service.armory.ArmoryContext;
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
import java.util.Set;

import static com.dasi.domain.ai.model.enumeration.AiType.*;

@Slf4j
@Service
public class ArmoryClientNode extends AbstractArmoryNode {

    private static final String DEFAULT_SYSTEM = "You are a helpful assistant.";

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {

        Map<String, AiPromptVO> aiPromptVOMap = armoryContext.getValue(PROMPT.getType());
        Set<AiClientVO> aiClientVOList = armoryContext.getValue(CLIENT.getType());

        if (aiClientVOList == null || aiClientVOList.isEmpty()) {
            return router(armoryRequestEntity, armoryContext);
        }

        for (AiClientVO aiClientVO : aiClientVOList) {

            // 1. 构建系统提示词
            StringBuilder system = new StringBuilder();
            List<String> promptIdList = aiClientVO.getPromptIdList();
            if (promptIdList != null && !promptIdList.isEmpty() && aiPromptVOMap != null) {
                for (String promptId : promptIdList) {
                    AiPromptVO aiPromptVO = aiPromptVOMap.get(promptId);
                    if (aiPromptVO != null && aiPromptVO.getPromptContent() != null) {
                        system.append(aiPromptVO.getPromptContent());
                    }
                }
            }
            if (system.isEmpty()) {
                system.append(DEFAULT_SYSTEM);
            }

            // 2. 拿到 Model
            String modelId = aiClientVO.getModelId();
            String modelBeanName = MODEL.getBeanName(modelId);
            OpenAiChatModel openAiChatModel = getBean(modelBeanName);

            // 3. 拿到 Mcp
            List<McpSyncClient> mcpSyncClientList = new ArrayList<>();
            List<String> mcpIdList = aiClientVO.getMcpIdList();
            if (mcpIdList != null && !mcpIdList.isEmpty()) {
                for (String mcpId : mcpIdList) {
                    String mcpBeanName = MCP.getBeanName(mcpId);
                    McpSyncClient mcpSyncClient = getBean(mcpBeanName);
                    if (mcpSyncClient != null) {
                        mcpSyncClientList.add(mcpSyncClient);
                    }
                }
            }
            SyncMcpToolCallbackProvider toolCallbackList = new SyncMcpToolCallbackProvider(mcpSyncClientList.toArray(new McpSyncClient[0]));

            // 4. 拿到 Advisor
            List<Advisor> advisorList = new ArrayList<>();
            List<String> advisorIdList = aiClientVO.getAdvisorIdList();
            if (advisorIdList != null && !advisorIdList.isEmpty()) {
                for (String advisorId : advisorIdList) {
                    String advisorBeanName = ADVISOR.getBeanName(advisorId);
                    Advisor advisor = getBean(advisorBeanName);
                    if (advisor != null) {
                        advisorList.add(advisor);
                    }
                }
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
            log.info("【装配节点】ArmoryClientNode：clientBeanName={}", clientBeanName);
        }

        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return defaultStrategyHandler;
    }

}
