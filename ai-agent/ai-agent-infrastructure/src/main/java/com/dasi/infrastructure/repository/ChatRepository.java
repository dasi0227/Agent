package com.dasi.infrastructure.repository;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.dasi.domain.chat.model.enumeration.AiMcpType;
import com.dasi.domain.chat.model.vo.AiMcpVO;
import com.dasi.domain.chat.repository.IChatRepository;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.infrastructure.redis.IRedisService;
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

import static com.dasi.types.common.RedisConstant.*;

@Repository
@Slf4j
public class ChatRepository implements IChatRepository {

    @Resource
    private IAiClientDao clientDao;

    @Resource
    private IAiMcpDao mcpDao;

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

        List<ChatClientResponse> chatClientResponseList = redisService.getValue(CHAT_CLIENT_LIST_KEY);
        if (chatClientResponseList != null) {
            return chatClientResponseList;
        }

        List<AiClient> aiClientList = clientDao.queryChatClientList();
        if (aiClientList == null || aiClientList.isEmpty()) {
            chatClientResponseList = new ArrayList<>();
            redisService.setValue(CHAT_CLIENT_LIST_KEY, chatClientResponseList);
            return chatClientResponseList;
        }

        chatClientResponseList = aiClientList.stream()
                .map(aiClient -> ChatClientResponse.builder()
                        .clientId(aiClient.getClientId())
                        .modelName(aiClient.getModelName())
                        .clientDesc(aiClient.getClientDesc())
                        .build())
                .toList();

        redisService.setValue(CHAT_CLIENT_LIST_KEY, chatClientResponseList);
        return chatClientResponseList;
    }


    @Override
    public List<ChatMcpResponse> queryChatMcpResponseList() {

        List<ChatMcpResponse> chatMcpResponseList = redisService.getValue(CHAT_MCP_LIST_KEY);
        if (chatMcpResponseList != null) {
            return chatMcpResponseList;
        }

        List<AiMcp> aiMcpList = mcpDao.queryChatMcpList();
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            chatMcpResponseList = new ArrayList<>();
            redisService.setValue(CHAT_MCP_LIST_KEY, chatMcpResponseList);
            return chatMcpResponseList;
        }

        chatMcpResponseList = aiMcpList.stream()
                .map(aiMcp -> ChatMcpResponse.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpDesc(aiMcp.getMcpDesc())
                        .build())
                .toList();

        redisService.setValue(CHAT_MCP_LIST_KEY, chatMcpResponseList);
        return chatMcpResponseList;
    }

    @Override
    public List<AiMcpVO> queryAiMcpVOListByMcpIdList(List<String> mcpIdList) {

        List<AiMcpVO> aiMcpVOList = new ArrayList<>();

        List<AiMcp> aiMcpList = mcpDao.queryByMcpIdList(mcpIdList);
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
    public List<String> queryRagTagList() {

        List<String> ragTagList = redisService.getValue(CHAT_RAG_TAG_LIST_KEY);
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

        redisService.setValue(CHAT_RAG_TAG_LIST_KEY, ragTagList);
        return ragTagList;
    }

}
