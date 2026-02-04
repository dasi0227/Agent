package com.dasi.domain.util.message;

public interface IMessageService {

    void saveUserMessage(String sessionId, String messageContent);

    void saveAssistantMessage(String sessionId, String messageContent);

    void saveWorkSseMessage(String sessionId, String messageContent);

    void saveWorkAnswerMessage(String sessionId, String messageRole, String messageContent);
}
