package com.dasi;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.dasi.type.SystemConstant.*;

@SpringBootApplication
@Configuration
@Slf4j
public class AgentApplication {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.ai.vectorstore.pgvector.schema-name:public}")
    private String vectorSchema;

    @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store_ollama}")
    private String vectorTable;

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class);
    }

    @PostConstruct
    public void initRedis() {
        RList<String> chatModelList = redissonClient.getList(REDIS_CHAT_MODEL_LIST_KEY);
        addIfMissing(chatModelList, "qwen2.5:7b");
        addIfMissing(chatModelList, "deepseek-r1:7b");
        addIfMissing(chatModelList, "doubao-seed-1.8");

        loadRagTags();
    }

    private void addIfMissing(RList<String> list, String model) {
        if (!list.contains(model)) {
            list.add(model);
        }
    }

    private void loadRagTags() {
        String schema = vectorSchema;
        String tableName = vectorTable;

        if (schema == null || schema.trim().isEmpty() || tableName == null || tableName.trim().isEmpty()) {
            log.warn("Skip rag tag init due to missing schema/table config. schema={}, table={}", schema, tableName);
            return;
        }

        String sql = String.format(
                "select distinct metadata->>'%s' as tag from %s.%s where metadata->>'%s' is not null",
                PGVECTOR_KNOWLEDGE_KEY, schema, tableName, PGVECTOR_KNOWLEDGE_KEY
        );

        try {
            List<String> tags = jdbcTemplate.queryForList(sql, String.class);
            if (tags.isEmpty()) {
                return;
            }

            RList<String> list = redissonClient.getList(REDIS_RAG_TAG_LIST_KEY);
            for (String tag : tags) {
                if (tag == null || tag.trim().isEmpty()) {
                    continue;
                }
                if (!list.contains(tag)) {
                    list.add(tag);
                }
            }
        } catch (DataAccessException ex) {
            log.warn("Failed to init rag tags from {}.{}: {}", schema, tableName, ex.getMessage());
        }
    }
}
