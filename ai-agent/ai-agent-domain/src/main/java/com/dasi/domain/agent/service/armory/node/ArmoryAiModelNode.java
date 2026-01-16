package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.model.vo.AiModelVO;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.*;

@Slf4j
@Service
public class ArmoryAiModelNode extends AbstractArmoryNode {

    @Resource
    private ArmoryAiAdvisorNode armoryAiAdvisorNode;

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {

        List<AiModelVO> aiModelVOList = dynamicContext.getValue(MODEL.getCode());

        if (aiModelVOList == null || aiModelVOList.isEmpty()) {
            log.warn("【装配节点】ArmoryAiModelNode：没有数据");
            return router(armoryCommandEntity, dynamicContext);
        }

        for (AiModelVO aiModelVO : aiModelVOList) {

            // 获取当前 Model 关联的 API
            String apiBeanName = API.getBeanName(aiModelVO.getApiId());
            OpenAiApi openAiApi = getBean(apiBeanName);
            if (openAiApi == null) {
                log.error("【装配节点】ArmoryAiModelNode：不存在 API");
            }

            // 获取当前 Model 关联的 MCP
            List<McpSyncClient> mcpSyncClientList = new ArrayList<>();
            for (String mcpId : aiModelVO.getMcpIdList()) {
                String mcpBeanName = MCP.getBeanName(mcpId);
                McpSyncClient mcpSyncClient = getBean(mcpBeanName);
                mcpSyncClientList.add(mcpSyncClient);
            }
            ToolCallback[] toolCallbacks = new SyncMcpToolCallbackProvider(mcpSyncClientList).getToolCallbacks();

            // 实例化
            OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                    .model(aiModelVO.getModelName())
                    .toolCallbacks(toolCallbacks)
                    .build();
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(openAiChatOptions)
                    .build();

            // 注册 Bean 对象
            String modelBeanName = MODEL.getBeanName(aiModelVO.getModelId());
            registerBean(modelBeanName, OpenAiChatModel.class, chatModel);
            log.info("【装配节点】ArmoryAiModelNode：modelBeanName={}, modelType={}, modelName={}", modelBeanName, aiModelVO.getModelType(), aiModelVO.getModelName());
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        return armoryAiAdvisorNode;
    }

}
