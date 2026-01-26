package com.dasi.domain.chat.service.rag;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;

import java.util.List;

public interface IAugmentService {

    List<Message> augmentRagMessage(String userMessage, String ragTag);

    void uploadRagFile();

    SyncMcpToolCallbackProvider augmentMcpTool(List<String> mcpIdList);

}
