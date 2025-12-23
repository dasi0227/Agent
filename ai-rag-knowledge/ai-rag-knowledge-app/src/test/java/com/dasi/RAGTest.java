package com.dasi;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RAGTest {

    @Resource
    private ChatClient chatClient;

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private PgVectorStore pgVectorStore;

    @Test
    public void upload() {
        // 读取本地文件为 Document 列表
        TikaDocumentReader reader = new TikaDocumentReader("./file.txt");

        // 执行文档读取
        List<Document> documentList = reader.get();

        // 使用分词器对文档做切分
        List<Document> documentSplitterList = tokenTextSplitter.apply(documentList);

        // 为原始文档打上知识库标识
        documentList.forEach(document -> document.getMetadata().put("data_source", "file.txt"));

        // 为切分后的文档打上知识库标识
        documentSplitterList.forEach(document -> document.getMetadata().put("data_source", "file.txt"));

        // 写入向量库
        pgVectorStore.add(documentSplitterList);

        log.info("上传完成");
    }

    @Test
    public void chat() {
        // 用户查询问题
        String message = "万驿苇是谁？";
        UserMessage userMessage = new UserMessage(message);

        // RAG 系统提示词模板
        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        // 构建向量检索请求
        String key = "data_source";
        String value = "file.txt";
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        Expression expression = filterExpressionBuilder.eq(key, value).build();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(message)
                .filterExpression(expression)
                .topK(5)
                .build();

        // 执行向量检索
        List<Document> documentList = pgVectorStore.similaritySearch(searchRequest);

        // 将检索结果合并为一个文本块，过滤空文档和空内容
        String documentCollectors = (documentList == null ? List.<Document>of() : documentList).stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        // 生成带占位符替换的系统消息
        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));

        // 组合系统消息与用户消息
        List<Message> messageList = new ArrayList<>();
        messageList.add(ragMessage);
        messageList.add(userMessage);

        // 调用 ChatClient 获取响应
        ChatOptions chatOptions = ChatOptions.builder().model("deepseek-r1:1.5b").build();
        ChatResponse chatResponse = chatClient
                .prompt()
                .messages(messageList)
                .options(chatOptions)
                .call()
                .chatResponse();

        // 输出响应结果
        log.info("测试结果:{}", chatResponse);

    }

}
