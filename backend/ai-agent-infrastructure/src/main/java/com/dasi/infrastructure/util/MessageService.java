package com.dasi.infrastructure.util;

import com.dasi.domain.session.model.enumeration.MessageType;
import com.dasi.domain.session.model.enumeration.SessionType;
import com.dasi.domain.util.message.IMessageService;
import com.dasi.infrastructure.persistent.dao.IMessageDao;
import com.dasi.infrastructure.persistent.dao.ISessionDao;
import com.dasi.infrastructure.persistent.po.Message;
import com.dasi.infrastructure.persistent.po.Session;
import com.dasi.types.exception.SessionException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MessageService implements IMessageService {

    private static final int CHAT_USER_LIMIT = 20;
    private static final int WORK_USER_LIMIT = 3;

    @Resource
    private IMessageDao messageDao;

    @Resource
    private ISessionDao sessionDao;

    @Override
    public void saveUserMessage(String sessionId, String messageContent) {
        saveMessage(sessionId, MessageType.CHAT.getType(), "user", messageContent);
    }

    @Override
    public void saveAssistantMessage(String sessionId, String messageContent) {
        saveMessage(sessionId, MessageType.CHAT.getType(), "assistant", messageContent);
    }

    @Override
    public void saveWorkSseMessage(String sessionId, String messageContent) {
        saveMessage(sessionId, MessageType.WORK_SSE.getType(), "assistant", messageContent);
    }

    @Override
    public void saveWorkAnswerMessage(String sessionId, String messageRole, String messageContent) {
        String role = StringUtils.hasText(messageRole) ? messageRole : "assistant";
        saveMessage(sessionId, MessageType.WORK_ANSWER.getType(), role, messageContent);
    }

    private void saveMessage(String sessionId, String messageType, String messageRole, String messageContent) {
        if (!StringUtils.hasText(sessionId)) {
            throw new SessionException("会话信息缺失");
        }
        Session session = sessionDao.queryById(sessionId);
        if (session == null) {
            throw new SessionException("会话不存在");
        }
        checkUserLimit(session, messageType, messageRole);
        Integer maxSeq = messageDao.maxSeqBySessionAndType(sessionId, messageType);
        int nextSeq = maxSeq == null ? 1 : maxSeq + 1;
        LocalDateTime now = LocalDateTime.now();
        Message message = Message.builder()
                .sessionId(sessionId)
                .messageContent(messageContent == null ? "" : messageContent)
                .messageRole(messageRole)
                .messageType(messageType)
                .messageSeq(nextSeq)
                .createTime(now)
                .updateTime(now)
                .build();
        messageDao.insert(message);
    }

    private void checkUserLimit(Session session, String messageType, String messageRole) {
        if (!"user".equalsIgnoreCase(messageRole)) {
            return;
        }
        SessionType sessionType = SessionType.fromType(session.getSessionType());
        if (sessionType == null) {
            return;
        }
        int limit = sessionType == SessionType.WORK ? WORK_USER_LIMIT : CHAT_USER_LIMIT;
        String countType = sessionType == SessionType.WORK ? MessageType.WORK_ANSWER.getType() : MessageType.CHAT.getType();
        Integer count = messageDao.countBySessionAndRoleAndType(session.getSessionId(), "user", countType);
        int current = count == null ? 0 : count;
        if (current >= limit) {
            throw new SessionException("当前会话已达到用户消息上限");
        }
    }
}
