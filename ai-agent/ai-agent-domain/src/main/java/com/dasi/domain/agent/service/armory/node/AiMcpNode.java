package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.model.enumeration.AiMcpType;
import com.dasi.domain.agent.model.vo.AiMcpVO;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.dasi.domain.agent.model.enumeration.AiEnum.MCP;

@Slf4j
@Service
public class AiMcpNode extends AbstractArmoryNode {

    @Resource
    private AiModelNode aiModelNode;

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {

        List<AiMcpVO> aiMcpVOList = dynamicContext.getValue(MCP.getCode());

        if (aiMcpVOList == null || aiMcpVOList.isEmpty()) {
            log.warn("【构建节点】AiMcpNode：没有数据");
            return router(armoryCommandEntity, dynamicContext);
        }

        for (AiMcpVO aiMcpVO : aiMcpVOList) {
            McpSyncClient mcpSyncClient = null;

            switch (AiMcpType.fromCode(aiMcpVO.getMcpType())) {
                case SSE -> {
                    AiMcpVO.SseConfig sseConfig = aiMcpVO.getSseConfig();
                    String baseUri = sseConfig.getBaseUri();
                    String sseEndPoint = sseConfig.getSseEndPoint();

                    HttpClientSseClientTransport sseClient = HttpClientSseClientTransport
                            .builder(baseUri)
                            .sseEndpoint(sseEndPoint)
                            .build();

                    mcpSyncClient = McpClient
                            .sync(sseClient)
                            .requestTimeout(Duration.ofMinutes(aiMcpVO.getMcpTimeout()))
                            .build();

                    mcpSyncClient.initialize();
                    log.info("【构建节点】AiMcpNode：sseConfig={}", sseConfig);
                }
                case STDIO -> {
                    AiMcpVO.StdioConfig stdioConfig = aiMcpVO.getStdioConfig();
                    Map<String, AiMcpVO.StdioConfig.Stdio> stdioMap = stdioConfig.getStdio();
                    AiMcpVO.StdioConfig.Stdio stdio = stdioMap.get(aiMcpVO.getMcpName());

                    ServerParameters stdioParams = ServerParameters
                            .builder(stdio.getCommand())
                            .args(stdio.getArgs())
                            .env(stdio.getEnv())
                            .build();

                    StdioClientTransport stdioClient = new StdioClientTransport(stdioParams);

                    mcpSyncClient = McpClient
                            .sync(stdioClient)
                            .requestTimeout(Duration.ofMinutes(aiMcpVO.getMcpTimeout()))
                            .build();

                    mcpSyncClient.initialize();
                    log.info("【构建节点】AiMcpNode：stdioConfig={}", stdioConfig);
                }
            }

            String mcpBeanName = MCP.getBeanName(aiMcpVO.getMcpId());
            registerBean(mcpBeanName, McpSyncClient.class, mcpSyncClient);
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        return aiModelNode;
    }

}
