package com.dasi.domain.chat.service.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagService implements IRagService {

    public static final String RAG_SYSTEM_PROMPT = """
            你是一个检索增强问答助手（RAG），你会收到一段参考资料（DOCUMENTS）。
            
            请严格遵守以下规则：
            - 事实依据：所有可核验的事实必须来自 DOCUMENTS；不要引入 DOCUMENTS 之外的具体事实、数字、名称、结论；
            - 推理允许：可以基于 DOCUMENTS 做必要的归纳、对比与推理，但必须明确区分“资料原文信息”与“你的推断”；
            - 冲突处理：若 DOCUMENTS 内部信息矛盾，指出矛盾点，并给出你认为更可信的依据；
            - 引用方式：以自然口吻作答，但关键结论要在句子中体现依据来自 CONTEXT 中的什么内容；
            - 输出约束：用简体中文回答，优先条目化，简洁直接；
            - 空处理：如果 DOCUMENTS 内容为空，就直接当作什么都没有提供，直接回答即可。
            
            DOCUMENTS:
            {documents}
            """;

    @Resource
    private PgVectorStore pgVectorStore;

    @Override
    public List<Message> addRagMessage(String userMessage, String ragTag) {

        if (ragTag == null || ragTag.isEmpty()) {
            return List.of(new UserMessage(userMessage));
        }

        // 构建向量检索条件
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        Filter.Expression expression = filterExpressionBuilder.eq("knowledge", ragTag).build();

        // 构建向量检索请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userMessage)
                .filterExpression(expression)
                .topK(5)
                .build();

        // 执行向量检索
        List<Document> documentList = pgVectorStore.similaritySearch(searchRequest);

        // 将检索结果合并为一个文本块，过滤空文档和空内容
        String documentString = (documentList == null ? List.<Document>of() : documentList).stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        // 用户消息 + 系统消息
        return List.of(
                new SystemPromptTemplate(RAG_SYSTEM_PROMPT).createMessage(Map.of("documents", documentString)),
                new UserMessage(userMessage)
        );
    }

    @Override
    public void uploadRagFile() {

    }
}
