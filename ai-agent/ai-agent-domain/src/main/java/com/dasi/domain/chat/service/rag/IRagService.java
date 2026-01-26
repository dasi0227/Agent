package com.dasi.domain.chat.service.rag;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface IRagService {

    List<Message> addRagMessage(String userMessage, String ragTag);

    void uploadRagFile();

}
