package com.dasi.mcp.tool;

import com.dasi.mcp.adapter.INoticeWeiXinPort;
import com.dasi.mcp.dto.NoticeWeComToolRequest;
import com.dasi.mcp.dto.NoticeWeComToolResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class NoticeWeComTool {

    @Resource
    private INoticeWeiXinPort noticeWeiXinPort;

    @Tool(description = "企业微信的应用消息通知")
    public NoticeWeComToolResponse noticeArticle(NoticeWeComToolRequest toolRequest) throws IOException {
        log.info("调用 MCP 工具进行企业微信应用消息通知：标题={} 概述={} 链接={}", toolRequest.getTitle(), toolRequest.getDescription(), toolRequest.getUrl());
        return noticeWeiXinPort.noticeArticle(toolRequest);
    }

}
