package com.dasi.http;

import com.dasi.IRagService;
import com.dasi.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static com.dasi.type.SystemConstant.*;

@RestController
@RequestMapping("/api/v1/rag")
@Slf4j
public class RagController implements IRagService {

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private PgVectorStore pgVectorStore;

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Result<Void> uploadFile(
            @RequestPart("ragTag") String ragTag,
            @RequestPart("fileList") List<MultipartFile> fileList) {

        for (MultipartFile file : fileList) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documentList = documentReader.get();
            List<Document> documentSplitList = tokenTextSplitter.apply(documentList);

            documentSplitList.forEach(document -> document.getMetadata().put(PGVECTOR_KNOWLEDGE_KEY, ragTag));
            writeToVectorStore(documentSplitList);

            RList<String> elements = redissonClient.getList(REDIS_RAG_TAG_LIST_KEY);
            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
            }
        }

        log.info("知识库上传完成：ragTag={}", ragTag);

        return Result.success();
    }

    @PostMapping("/git")
    @Override
    public Result<Void> uploadGitRepo(@RequestParam String repo, @RequestParam String username, @RequestParam String password) {
        try {
            String[] parts = repo.split("/");
            String projectName = parts[parts.length - 1].replace(".git", "");

            File path = new File(GIT_CLONE_DIRECTORY);
            FileUtils.deleteDirectory(path);
            log.info("克隆地址={}，克隆项目={}", path.getAbsolutePath(), projectName);

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
            Git git = Git.cloneRepository()
                    .setURI(repo)
                    .setDirectory(path)
                    .setCredentialsProvider(credentialsProvider)
                    .call();

            Path root = Paths.get(GIT_CLONE_DIRECTORY);
            Files.walkFileTree(root, new SimpleFileVisitor<>() {

                // 跳过 .git 及其所有子目录
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName() != null && ".git".equals(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!attrs.isRegularFile()) {
                        return FileVisitResult.CONTINUE;
                    }

                    if (!isTextLikeFile(file)) {
                        return FileVisitResult.CONTINUE;
                    }

                    log.info("正在上传文件：{}", file);

                    PathResource resource = new PathResource(file);
                    TikaDocumentReader reader = new TikaDocumentReader(resource);

                    List<Document> documents = reader.get();
                    List<Document> split = tokenTextSplitter.apply(documents);
                    split.forEach(doc -> doc.getMetadata().put("knowledge", projectName));

                    writeToVectorStore(split);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.info("访问文件失败：file={}, error={}", file.toString(), exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

            RList<String> elements = redissonClient.getList(REDIS_RAG_TAG_LIST_KEY);
            if (!elements.contains(projectName)) {
                elements.add(projectName);
            }

            git.close();

            FileUtils.deleteDirectory(path);

            log.info("知识库上传完成：ragTag={}", projectName);

            return Result.success();

        } catch (IOException | GitAPIException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static boolean isTextLikeFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();

        // 黑名单
        if (name.endsWith(".pdf") || name.endsWith(".png") || name.endsWith(".jpg")
                || name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".bmp")
                || name.endsWith(".ico") || name.endsWith(".svg") || name.endsWith(".jpeg"))
            return false;

        // 白名单
        return name.endsWith(".java") || name.endsWith(".yml") || name.endsWith(".xml")
                || name.endsWith(".html") || name.endsWith(".js") || name.endsWith(".css")
                || name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".conf");
    }

    private void writeToVectorStore(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        pgVectorStore.add(documents);
    }

}
