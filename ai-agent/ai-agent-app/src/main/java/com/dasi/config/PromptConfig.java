package com.dasi.config;

import com.dasi.infrastructure.persistent.dao.IAiPromptDao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PromptConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private IAiPromptDao aiPromptDao;

    private static final Map<String, String> PROMPT_FILE_MAP = Map.of(
            "prompt_analyzer_1", "analyzer.txt",
            "prompt_performer_1", "performer.txt",
            "prompt_supervisor_1", "supervisor.txt",
            "prompt_summarizer_1", "summarizer.txt"
    );

    private static final Path PROMPT_DIR =
            Paths.get("/Users/wyw/Desktop/Project/Agent/ai-agent/docs/prompt");

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        PROMPT_FILE_MAP.forEach((promptId, fileName) -> {
            try {
                Path path = PROMPT_DIR.resolve(fileName);
                String content = Files.readString(path, StandardCharsets.UTF_8);

                if (!org.springframework.util.StringUtils.hasText(content)) {
                    throw new IllegalStateException("Prompt 内容为空: " + promptId);
                }

                aiPromptDao.updatePromptContent(promptId, content);
                log.info("【初始化配置】promptId={}", promptId);

            } catch (IOException e) {
                throw new RuntimeException("读取 Prompt 文件失败: " + fileName, e);
            }
        });
    }
}
