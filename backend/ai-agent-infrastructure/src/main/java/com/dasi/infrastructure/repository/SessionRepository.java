package com.dasi.infrastructure.repository;

import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.session.repository.ISessionRepository;
import com.dasi.infrastructure.persistent.dao.IMessageDao;
import com.dasi.infrastructure.persistent.dao.ISessionDao;
import com.dasi.infrastructure.persistent.po.Message;
import com.dasi.infrastructure.persistent.po.Session;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class SessionRepository implements ISessionRepository {

    @Resource
    private ISessionDao sessionDao;

    @Resource
    private IMessageDao messageDao;

    @Override
    public List<SessionVO> listSession(String sessionUser) {
        List<Session> list = sessionDao.queryByUser(sessionUser);
        return list.stream().map(this::toSessionVO).toList();
    }

    @Override
    public int countSessionByType(String sessionUser, String sessionType) {
        return sessionDao.countByUserAndType(sessionUser, sessionType);
    }

    @Override
    public void insertSession(String sessionId, String sessionUser, String sessionTitle, String sessionType) {
        Session session = Session.builder()
                .sessionId(sessionId)
                .sessionUser(sessionUser)
                .sessionTitle(sessionTitle)
                .sessionType(sessionType)
                .build();
        sessionDao.insert(session);
    }

    @Override
    public void updateSessionTitle(Long id, String sessionTitle) {
        sessionDao.updateTitle(id, sessionTitle);
    }

    @Override
    public void deleteSession(Long id) {
        sessionDao.delete(id);
    }

    @Override
    public void deleteMessagesBySessionId(String sessionId) {
        messageDao.deleteBySessionId(sessionId);
    }

    @Override
    public SessionVO querySessionById(Long id) {
        return toSessionVO(sessionDao.queryById(id));
    }

    @Override
    public SessionVO querySessionBySessionId(String sessionId) {
        return toSessionVO(sessionDao.queryBySessionId(sessionId));
    }

    @Override
    public List<MessageVO> listMessageBySessionAndType(String sessionId, String messageType) {
        List<Message> list = messageDao.queryBySessionAndType(sessionId, messageType);
        return list.stream().map(this::toMessageVO).toList();
    }

    private SessionVO toSessionVO(Session session) {
        if (session == null) {
            return null;
        }
        return SessionVO.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .sessionUser(session.getSessionUser())
                .sessionTitle(session.getSessionTitle())
                .sessionType(session.getSessionType())
                .createTime(session.getCreateTime())
                .build();
    }

    private MessageVO toMessageVO(Message message) {
        if (message == null) {
            return null;
        }
        return MessageVO.builder()
                .id(message.getId())
                .messageContent(message.getMessageContent())
                .messageRole(message.getMessageRole())
                .messageType(message.getMessageType())
                .messageSeq(message.getMessageSeq())
                .createTime(message.getCreateTime())
                .build();
    }
}
