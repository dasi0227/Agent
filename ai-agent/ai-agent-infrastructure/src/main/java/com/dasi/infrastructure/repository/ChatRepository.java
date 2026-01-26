package com.dasi.infrastructure.repository;

import com.dasi.domain.chat.repository.IChatRepository;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.redis.IRedisService;
import com.dasi.types.dto.response.ChatClientResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.types.common.RedisConstant.CHAT_CLIENT_LIST_KEY;
import static com.dasi.types.common.RedisConstant.RAG_TAG_LIST_KEY;

@Repository
@Slf4j
public class ChatRepository implements IChatRepository {

    @Resource
    private IAiClientDao aiClientDao;

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

        List<AiClient> aiClientList = aiClientDao.queryChatClientList();
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
    public List<String> queryRagTagList() {

        List<String> ragTagList = redisService.getValue(RAG_TAG_LIST_KEY);
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

        redisService.setValue(RAG_TAG_LIST_KEY, ragTagList);
        return ragTagList;
    }

}
