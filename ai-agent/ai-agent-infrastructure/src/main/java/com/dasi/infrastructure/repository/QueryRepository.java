package com.dasi.infrastructure.repository;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.dasi.domain.query.model.enumeration.AiMcpType;
import com.dasi.domain.query.model.vo.AiMcpVO;
import com.dasi.domain.query.repository.IQueryRepository;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.domain.util.IRedisService;
import com.dasi.types.dto.response.WorkAgentResponse;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.response.ChatMcpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dasi.types.constant.RedisConstant.*;

@Repository
@Slf4j
public class QueryRepository implements IQueryRepository {

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiMcpDao aiMcpDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IRedisService redisService;

    @Resource(name = "postgresqlTemplate")
    private JdbcTemplate jdbcTemplate;

    @Value("${openai.embedding.schema-name}")
    private String embeddingSchemaName;

    @Value("${openai.embedding.table-name}")
    private String embeddingTableName;

    @Override
    public List<ChatClientResponse> queryChatClientResponseList() {

        List<ChatClientResponse> chatClientResponseList = redisService.getList(LIST_CHAT_CLIENT_KEY, ChatClientResponse.class);
        if (chatClientResponseList != null) {
            return chatClientResponseList;
        }

        List<AiClient> aiClientList = aiClientDao.queryChatClientList();
        if (aiClientList == null || aiClientList.isEmpty()) {
            chatClientResponseList = new ArrayList<>();
            redisService.setList(LIST_CHAT_CLIENT_KEY, chatClientResponseList);
            return chatClientResponseList;
        }

        chatClientResponseList = aiClientList.stream()
                .map(aiClient -> ChatClientResponse.builder()
                        .clientId(aiClient.getClientId())
                        .modelName(aiClient.getModelName())
                        .clientDesc(aiClient.getClientDesc())
                        .build())
                .toList();

        redisService.setList(LIST_CHAT_CLIENT_KEY, chatClientResponseList);
        return chatClientResponseList;
    }


    @Override
    public List<ChatMcpResponse> queryChatMcpResponseList() {

        List<ChatMcpResponse> chatMcpResponseList = redisService.getList(LIST_CHAT_MCP_KEY, ChatMcpResponse.class);
        if (chatMcpResponseList != null) {
            return chatMcpResponseList;
        }

        List<AiMcp> aiMcpList = aiMcpDao.queryChatMcpList();
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            chatMcpResponseList = new ArrayList<>();
            redisService.setList(LIST_CHAT_MCP_KEY, chatMcpResponseList);
            return chatMcpResponseList;
        }

        chatMcpResponseList = aiMcpList.stream()
                .map(aiMcp -> ChatMcpResponse.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpDesc(aiMcp.getMcpDesc())
                        .build())
                .toList();

        redisService.setList(LIST_CHAT_MCP_KEY, chatMcpResponseList);
        return chatMcpResponseList;
    }

    @Override
    public List<AiMcpVO> queryAiMcpVOListByMcpIdList(List<String> mcpIdList) {

        List<AiMcpVO> aiMcpVOList = new ArrayList<>();

        List<AiMcp> aiMcpList = aiMcpDao.queryByMcpIdList(mcpIdList);
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            return aiMcpVOList;
        }

        for (AiMcp aiMcp : aiMcpList) {
            AiMcpVO aiMcpVO = AiMcpVO.builder()
                    .mcpId(aiMcp.getMcpId())
                    .mcpName(aiMcp.getMcpName())
                    .mcpType(aiMcp.getMcpType())
                    .mcpConfig(aiMcp.getMcpConfig())
                    .mcpTimeout(aiMcp.getMcpTimeout())
                    .build();

            try {
                switch (AiMcpType.fromCode(aiMcp.getMcpType())) {
                    case SSE -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        AiMcpVO.SseConfig sseConfig = objectMapper.readValue(aiMcp.getMcpConfig(), AiMcpVO.SseConfig.class);
                        aiMcpVO.setSseConfig(sseConfig);
                    }
                    case STDIO -> {
                        Map<String, AiMcpVO.StdioConfig.Stdio> stdio = JSON.parseObject(aiMcp.getMcpConfig(), new TypeReference<>() {});
                        AiMcpVO.StdioConfig stdioConfig = new AiMcpVO.StdioConfig();
                        stdioConfig.setStdio(stdio);
                        aiMcpVO.setStdioConfig(stdioConfig);
                    }
                }
                aiMcpVOList.add(aiMcpVO);
            } catch (Exception e) {
                log.error("【查询数据】失败：{}", e.getMessage());
                throw new IllegalStateException(e);
            }
        }

        return aiMcpVOList;
    }

    @Override
    public List<WorkAgentResponse> queryAgentResponseList() {

        List<WorkAgentResponse> workAgentResponseList = redisService.getList(LIST_WORK_AGENT_KEY, WorkAgentResponse.class);
        if (workAgentResponseList != null) {
            return workAgentResponseList;
        }

        List<AiAgent> aiAgentList = aiAgentDao.queryAgentList();
        if (aiAgentList == null || aiAgentList.isEmpty()) {
            workAgentResponseList = new ArrayList<>();
            redisService.setList(LIST_WORK_AGENT_KEY, workAgentResponseList);
            return workAgentResponseList;
        }

        workAgentResponseList = aiAgentList.stream()
                .map(aiAgent -> WorkAgentResponse.builder()
                        .agentId(aiAgent.getAgentId())
                        .agentName(aiAgent.getAgentName())
                        .agentDesc(aiAgent.getAgentDesc())
                        .build())
                .toList();

        redisService.setList(LIST_WORK_AGENT_KEY, workAgentResponseList);
        return workAgentResponseList;
    }

    @Override
    public List<String> queryRagTagList() {

        List<String> ragTagList = redisService.getList(LIST_CHAT_RAG_KEY, String.class);
        if (ragTagList != null) {
            return ragTagList;
        }

        String tableRef = embeddingSchemaName + "." + embeddingTableName;
        String sql = """
                SELECT DISTINCT metadata::jsonb->>'knowledge' AS knowledge
                FROM %s
                WHERE metadata::jsonb ? 'knowledge' AND metadata::jsonb->>'knowledge' <> ''
                """
                .formatted(tableRef);
        ragTagList = jdbcTemplate.queryForList(sql, String.class);

        redisService.setList(LIST_CHAT_RAG_KEY, ragTagList);
        return ragTagList;
    }

}
