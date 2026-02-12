package com.dasi.domain.ai.service.rag;

import com.dasi.types.annotation.CacheEvict;
import com.dasi.types.dto.request.ai.AiUploadRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dasi.types.constant.RedisConstant.QUERY_CHAT_RAG_KEY;

@Slf4j
@Service
public class RagService implements IRagService {

    public static final String PGVECTOR_KNOWLEDGE_KEY = "knowledge";

    public static final String GIT_CLONE_DIRECTORY = "./git-cloned-repo";

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private PgVectorStore pgVectorStore;

    @Override
    @CacheEvict(keyPrefix = { QUERY_CHAT_RAG_KEY })
    public void uploadTextFile(String ragTag, List<MultipartFile> fileList) {

        if (fileList == null || fileList.isEmpty()) {
            log.warn("【上传知识库】上传文件失败：fileList 为空");
            return;
        }

        int successCount = 0;
        for (MultipartFile file : fileList) {
            if (addPgVectorStore(ragTag, file)) {
                successCount++;
            }
        }

        if (successCount == 0) {
            throw new IllegalStateException("知识库入库失败，请检查 Embedding 服务配置后重试");
        }

        if (successCount < fileList.size()) {
            log.warn("【上传知识库】部分文件入库失败：ragTag={}, total={}, success={}", ragTag, fileList.size(), successCount);
        }

    }

    @Override
    @CacheEvict(keyPrefix = { QUERY_CHAT_RAG_KEY })
    public void uploadGitRepo(AiUploadRequest aiUploadRequest) {

        String repoUrl = aiUploadRequest.getRepoUrl();
        String username = aiUploadRequest.getUsername();
        String password = aiUploadRequest.getPassword();

        try {
            String[] parts = repoUrl.split("/");
            String ragTag = parts[parts.length - 1].replace(".git", "");
            if (ragTag.isEmpty()) {
                log.warn("【上传知识库】上传 Git 仓库失败：repoUrl 不符合 .git 后缀要求");
                return;
            }

            String repoDirectory = GIT_CLONE_DIRECTORY + "/" + System.currentTimeMillis();
            File directory = new File(repoDirectory);
            if (directory.exists()) {
                FileUtils.deleteDirectory(directory);
            }

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
            try {
                AtomicInteger totalCount = new AtomicInteger(0);
                AtomicInteger successCount = new AtomicInteger(0);
                Git git = Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(directory)
                        .setCredentialsProvider(credentialsProvider)
                        .call();

                Path root = Paths.get(repoDirectory);
                Files.walkFileTree(root, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    Path path = dir.getFileName();
                    if (path == null) {
                        return FileVisitResult.CONTINUE;
                    }

                    String name = path.toString();
                    if (name.startsWith(".")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!attrs.isRegularFile()) {
                        return FileVisitResult.CONTINUE;
                    }

                    String fileName = file.getFileName().toString().toLowerCase();
                    if (fileName.endsWith(".java") || fileName.endsWith(".html") || fileName.endsWith(".md") || fileName.endsWith(".txt")) {
                        totalCount.incrementAndGet();
                        if (addPgVectorStore(ragTag, file)) {
                            successCount.incrementAndGet();
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException ex) {
                    String fileName = file.getFileName().toString().toLowerCase();
                    log.error("【上传知识库】入库失败：ragTag={}, fileName={}, error={}", ragTag, fileName, ex.getMessage(), ex);
                    return FileVisitResult.CONTINUE;
                }
                });

                if (totalCount.get() == 0) {
                    throw new IllegalStateException("未找到可入库文件，请检查仓库内容");
                }

                if (successCount.get() == 0) {
                    throw new IllegalStateException("知识库入库失败，请检查 Embedding 服务配置后重试");
                }

                if (successCount.get() < totalCount.get()) {
                    log.warn("【上传知识库】部分文件入库失败：ragTag={}, total={}, success={}", ragTag, totalCount.get(), successCount.get());
                }

                git.close();
            } finally {
                if (directory.exists()) {
                    FileUtils.deleteDirectory(directory);
                }
            }
        } catch (Exception e) {
            log.error("【上传知识库】上传 Git 仓库失败：error={}", e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private boolean addPgVectorStore(String ragTag, org.springframework.core.io.Resource resource, String fileName) {
        try {
            TikaDocumentReader reader = new TikaDocumentReader(resource);

            List<Document> documentList = reader.get();
            List<Document> documentSplitList = tokenTextSplitter.apply(documentList);
            if (documentSplitList == null || documentSplitList.isEmpty()) {
                log.warn("【上传知识库】文档切分后为空：ragTag={}, fileName={}", ragTag, fileName);
                return false;
            }

            documentSplitList.forEach(doc -> doc.getMetadata().put(PGVECTOR_KNOWLEDGE_KEY, ragTag));
            pgVectorStore.add(documentSplitList);

            log.info("【上传知识库】入库成功：ragTag={}, fileName={}", ragTag, fileName);
            return true;
        } catch (Exception e) {
            log.error("【上传知识库】入库失败：ragTag={}, fileName={}, error={}", ragTag, fileName, e.getMessage(), e);
            return false;
        }
    }

    private boolean addPgVectorStore(String ragTag, Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return addPgVectorStore(ragTag, new PathResource(file), fileName);
    }

    private boolean addPgVectorStore(String ragTag, MultipartFile file) {
        String fileName = (file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename().toLowerCase());
        return addPgVectorStore(ragTag, file.getResource(), fileName);
    }

}
