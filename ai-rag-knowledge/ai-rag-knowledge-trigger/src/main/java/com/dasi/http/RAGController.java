package com.dasi.http;

import com.dasi.IRAGService;
import com.dasi.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rag")
@Slf4j
public class RAGController implements IRAGService {

    private final String REDIS_RAG_TAG_LIST_KEY = "ragTagList";

    private final String PGVECTOR_KNOWLEDGE_KEY = "knowledge";

    @Resource
    private ChatClient chatClient;

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    private RedissonClient redissonClient;

    @GetMapping("/query/tags")
    @Override
    public Result<List<String>> queryRagTagList() {
        RList<String> elements = redissonClient.getList(REDIS_RAG_TAG_LIST_KEY);
        return Result.success(elements);
    }

    @PostMapping("/upload")
    @Override
    public Result<Void> uploadFile(@RequestParam String ragTag, @RequestParam List<MultipartFile> fileList) {
        for (MultipartFile file : fileList) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documentList = documentReader.get();
            List<Document> documentSplitList = tokenTextSplitter.apply(documentList);
            documentSplitList.forEach(document -> document.getMetadata().put(PGVECTOR_KNOWLEDGE_KEY, ragTag));
            pgVectorStore.accept(documentSplitList);
            RList<String> elements = redissonClient.getList(REDIS_RAG_TAG_LIST_KEY);
            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
            }
        }

        log.info("知识库上传完成：ragTag={}", ragTag);

        return Result.success();
    }

}
