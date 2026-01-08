package com.dasi.mcp.tool;

import com.dasi.mcp.adapter.IPostCsdnPort;
import com.dasi.mcp.dto.PostCsdnToolRequest;
import com.dasi.mcp.dto.PostCsdnToolResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostCsdnTool {

    @Resource
    private IPostCsdnPort postCsdnPort;

    @Tool(description = "发布文章到 CSDN")
    public PostCsdnToolResponse saveArticle(PostCsdnToolRequest toolRequest) {
        log.info("通过 MCP 向 CSDN 发帖：标签={}, 标题={}", toolRequest.getTags(), toolRequest.getTitle());
        return postCsdnPort.saveArticle(toolRequest);
    }

}
