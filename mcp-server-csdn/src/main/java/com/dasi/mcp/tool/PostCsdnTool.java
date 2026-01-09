package com.dasi.mcp.tool;

import com.dasi.mcp.adapter.IPostCsdnPort;
import com.dasi.mcp.dto.PostCsdnToolRequest;
import com.dasi.mcp.dto.PostCsdnToolResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class PostCsdnTool {

    @Resource
    private IPostCsdnPort postCsdnPort;

    @Tool(description = "发布文章到 CSDN")
    public PostCsdnToolResponse saveArticle(PostCsdnToolRequest toolRequest) throws IOException {
        log.info("调用 MCP 工具进行 CSDN 发帖：标题={}", toolRequest.getTitle());
        return postCsdnPort.saveArticle(toolRequest);
    }

}
