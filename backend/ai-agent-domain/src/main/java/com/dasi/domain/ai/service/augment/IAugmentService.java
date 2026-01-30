package com.dasi.domain.ai.service.augment;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;

import java.util.List;

public interface IAugmentService {

    List<Message> augmentRagMessage(String userMessage, String ragTag);

    SyncMcpToolCallbackProvider augmentMcpTool(List<String> mcpIdList);

}
