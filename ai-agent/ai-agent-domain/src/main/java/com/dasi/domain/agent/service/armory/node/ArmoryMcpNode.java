package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.enumeration.AiMcpType;
import com.dasi.domain.agent.model.vo.AiMcpVO;
import com.dasi.domain.agent.service.armory.ArmoryContext;
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

import static com.dasi.domain.agent.model.enumeration.AiType.MCP;

@Slf4j
@Service
public class ArmoryMcpNode extends AbstractArmoryNode {

    @Resource
    private ArmoryAdvisorNode armoryAdvisorNode;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {

        List<AiMcpVO> aiMcpVOList = armoryContext.getValue(MCP.getType());

        if (aiMcpVOList == null || aiMcpVOList.isEmpty()) {
            log.warn("【装配节点】ArmoryMcpNode：没有数据");
            return router(armoryRequestEntity, armoryContext);
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
                }
                case STDIO -> {
                    AiMcpVO.StdioConfig stdioConfig = aiMcpVO.getStdioConfig();
                    Map<String, AiMcpVO.StdioConfig.Stdio> stdioMap = stdioConfig.getStdio();
                    AiMcpVO.StdioConfig.Stdio stdio = stdioMap.get(aiMcpVO.getMcpId());

                    ServerParameters serverParameters = ServerParameters
                            .builder(stdio.getCommand())
                            .args(stdio.getArgs())
                            .env(stdio.getEnv())
                            .build();

                    StdioClientTransport stdioClient = new StdioClientTransport(serverParameters);

                    mcpSyncClient = McpClient
                            .sync(stdioClient)
                            .requestTimeout(Duration.ofMinutes(aiMcpVO.getMcpTimeout()))
                            .build();

                    mcpSyncClient.initialize();
                }
            }

            String mcpBeanName = MCP.getBeanName(aiMcpVO.getMcpId());
            registerBean(mcpBeanName, McpSyncClient.class, mcpSyncClient);
            log.info("【装配节点】ArmoryMcpNode：mcpBeanName={}, mcpType={}", mcpBeanName, aiMcpVO.getMcpType());
        }

        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return armoryAdvisorNode;
    }

}
