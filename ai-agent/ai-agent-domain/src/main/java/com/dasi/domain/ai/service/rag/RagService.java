package com.dasi.domain.ai.service.rag;

import com.dasi.types.dto.request.UploadGitRepoRequest;
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
    public void uploadTextFile(String ragTag, List<MultipartFile> fileList) {

        if (fileList == null || fileList.isEmpty()) {
            log.warn("【上传知识库】上传文件失败：fileList 为空");
            return;
        }

        try {
            for (MultipartFile file : fileList) {
                addPgVectorStore(ragTag, file);
            }
        } catch (Exception e) {
            log.error("【上传知识库】上传文件失败：error={}", e.getMessage(), e);
        }

    }

    @Override
    public void uploadGitRepo(UploadGitRepoRequest uploadGitRepoRequest) {

        String repoUrl = uploadGitRepoRequest.getRepoUrl();
        String username = uploadGitRepoRequest.getUsername();
        String password = uploadGitRepoRequest.getPassword();

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
                        addPgVectorStore(ragTag, file);
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

                git.close();
            } finally {
                if (directory.exists()) {
                    FileUtils.deleteDirectory(directory);
                }
            }
        } catch (Exception e) {
            log.error("【上传知识库】上传 Git 仓库失败：error={}", e.getMessage(), e);
        }
    }

    private void addPgVectorStore(String ragTag, org.springframework.core.io.Resource resource, String fileName) {
        try {
            TikaDocumentReader reader = new TikaDocumentReader(resource);

            List<Document> documentList = reader.get();
            List<Document> documentSplitList = tokenTextSplitter.apply(documentList);
            if (documentSplitList == null || documentSplitList.isEmpty()) {
                return;
            }

            documentSplitList.forEach(doc -> doc.getMetadata().put(PGVECTOR_KNOWLEDGE_KEY, ragTag));
            pgVectorStore.add(documentSplitList);

            log.info("【上传知识库】入库成功：ragTag={}, fileName={}", ragTag, fileName);
        } catch (Exception e) {
            log.error("【上传知识库】入库失败：ragTag={}, fileName={}, error={}", ragTag, fileName, e.getMessage(), e);
        }
    }

    private void addPgVectorStore(String ragTag, Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        addPgVectorStore(ragTag, new PathResource(file), fileName);
    }

    private void addPgVectorStore(String ragTag, MultipartFile file) {
        String fileName = (file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename().toLowerCase());
        addPgVectorStore(ragTag, file.getResource(), fileName);
    }

}
