package com.dasi.infrastructure.repository;

import com.dasi.domain.query.repository.IQueryRepository;
import com.dasi.domain.util.IRedisService;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.types.dto.response.QueryChatClientResponse;
import com.dasi.types.dto.response.QueryChatMcpResponse;
import com.dasi.types.dto.response.QueryChatRagResponse;
import com.dasi.types.dto.response.QueryWorkAgentResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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
    public List<QueryChatClientResponse> queryChatClientResponseList() {

        List<QueryChatClientResponse> queryChatClientResponseList = redisService.getList(LIST_CHAT_CLIENT_KEY, QueryChatClientResponse.class);
        if (queryChatClientResponseList != null) {
            return queryChatClientResponseList;
        }

        List<AiClient> aiClientList = aiClientDao.queryChatClientList();
        if (aiClientList == null || aiClientList.isEmpty()) {
            queryChatClientResponseList = new ArrayList<>();
            redisService.setList(LIST_CHAT_CLIENT_KEY, queryChatClientResponseList);
            return queryChatClientResponseList;
        }

        queryChatClientResponseList = aiClientList.stream()
                .map(aiClient -> QueryChatClientResponse.builder()
                        .clientId(aiClient.getClientId())
                        .modelName(aiClient.getModelName())
                        .clientDesc(aiClient.getClientDesc())
                        .build())
                .toList();

        redisService.setList(LIST_CHAT_CLIENT_KEY, queryChatClientResponseList);
        return queryChatClientResponseList;
    }


    @Override
    public List<QueryChatMcpResponse> queryChatMcpResponseList() {

        List<QueryChatMcpResponse> queryChatMcpResponseList = redisService.getList(LIST_CHAT_MCP_KEY, QueryChatMcpResponse.class);
        if (queryChatMcpResponseList != null) {
            return queryChatMcpResponseList;
        }

        List<AiMcp> aiMcpList = aiMcpDao.queryChatMcpList();
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            queryChatMcpResponseList = new ArrayList<>();
            redisService.setList(LIST_CHAT_MCP_KEY, queryChatMcpResponseList);
            return queryChatMcpResponseList;
        }

        queryChatMcpResponseList = aiMcpList.stream()
                .map(aiMcp -> QueryChatMcpResponse.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpDesc(aiMcp.getMcpDesc())
                        .build())
                .toList();

        redisService.setList(LIST_CHAT_MCP_KEY, queryChatMcpResponseList);
        return queryChatMcpResponseList;
    }

    @Override
    public List<QueryWorkAgentResponse> queryWorkAgentResponseList() {

        List<QueryWorkAgentResponse> queryWorkAgentResponseList = redisService.getList(LIST_WORK_AGENT_KEY, QueryWorkAgentResponse.class);
        if (queryWorkAgentResponseList != null) {
            return queryWorkAgentResponseList;
        }

        List<AiAgent> aiAgentList = aiAgentDao.queryAgentList();
        if (aiAgentList == null || aiAgentList.isEmpty()) {
            queryWorkAgentResponseList = new ArrayList<>();
            redisService.setList(LIST_WORK_AGENT_KEY, queryWorkAgentResponseList);
            return queryWorkAgentResponseList;
        }

        queryWorkAgentResponseList = aiAgentList.stream()
                .map(aiAgent -> QueryWorkAgentResponse.builder()
                        .agentId(aiAgent.getAgentId())
                        .agentName(aiAgent.getAgentName())
                        .agentDesc(aiAgent.getAgentDesc())
                        .build())
                .toList();

        redisService.setList(LIST_WORK_AGENT_KEY, queryWorkAgentResponseList);
        return queryWorkAgentResponseList;
    }

    @Override
    public List<QueryChatRagResponse> queryChatRagList() {

        List<QueryChatRagResponse> queryChatRagResponseList = redisService.getList(LIST_CHAT_RAG_KEY, QueryChatRagResponse.class);
        if (queryChatRagResponseList != null) {
            return queryChatRagResponseList;
        }

        String tableRef = embeddingSchemaName + "." + embeddingTableName;
        String sql = """
                SELECT DISTINCT metadata::jsonb->>'knowledge' AS knowledge
                FROM %s
                WHERE metadata::jsonb ? 'knowledge' AND metadata::jsonb->>'knowledge' <> ''
                """
                .formatted(tableRef);
        List<String> ragTagList = jdbcTemplate.queryForList(sql, String.class);
        queryChatRagResponseList = ragTagList.stream()
                .map(ragTag -> QueryChatRagResponse.builder().ragTag(ragTag).build())
                .toList();

        redisService.setList(LIST_CHAT_RAG_KEY, queryChatRagResponseList);
        return queryChatRagResponseList;
    }

}
