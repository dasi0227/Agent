package com.dasi.mcp.tool;

import com.dasi.mcp.dto.SendTextToolRequest;
import com.dasi.mcp.port.IWeComPort;
import com.dasi.mcp.dto.SendTextCardToolRequest;
import com.dasi.mcp.dto.SendMessageToolResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class WeComTool {

    @Resource
    private IWeComPort weComPort;

    @Tool(description = "企业微信发送应用消息，类型为文本卡片")
    public SendMessageToolResponse sendTextCard(SendTextCardToolRequest toolRequest) throws IOException {
        log.info("调用 MCP 工具进行企业微信应用文本卡片消息：标题={} 概述={} 链接={}", toolRequest.getTitle(), toolRequest.getDescription(), toolRequest.getUrl());
        return weComPort.sendTextCard(toolRequest);
    }

    @Tool(description = "企业微信发送应用消息，类型为纯文本")
    public SendMessageToolResponse sendText(SendTextToolRequest toolRequest) throws IOException {
        log.info("调用 MCP 工具进行企业微信应用文本消息：内容={}", toolRequest.getContent());
        return weComPort.sendText(toolRequest);
    }

}
