package com.dasi.http;

import com.dasi.IRAGService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rag")
@Slf4j
public class RAGController implements IRAGService {

    private final String REDIS_RAG_TAG_LIST_KEY = "ragTagList";

    private final String PGVECTOR_KNOWLEDGE_KEY = "knowledge";

    private final String GIT_CLONE_DIRECTORY = "./git-cloned-repo";

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

    @PostMapping("/analyze-git")
    @Override
    public Result<Void> analyzeGitRepo(String repo, String username, String password) {
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

                    pgVectorStore.accept(split);

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

            return Result.success();

        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
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
        return name.endsWith(".java") || name.endsWith(".kt") || name.endsWith(".go")
                || name.endsWith(".py") || name.endsWith(".js") || name.endsWith(".ts")
                || name.endsWith(".jsx") || name.endsWith(".tsx")
                || name.endsWith(".xml") || name.endsWith(".yml") || name.endsWith(".yaml")
                || name.endsWith(".json") || name.endsWith(".properties") || name.endsWith(".conf")
                || name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".sql")
                || name.endsWith(".sh") || name.endsWith(".bat") || name.endsWith(".gradle")
                || name.equals("dockerfile") || name.endsWith(".gitignore");
    }

}
