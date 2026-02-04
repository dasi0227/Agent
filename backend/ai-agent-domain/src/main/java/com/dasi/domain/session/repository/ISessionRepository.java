package com.dasi.domain.session.repository;

import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;

import java.util.List;

public interface ISessionRepository {

    List<SessionVO> listSession(String sessionUser);

    int countSessionByType(String sessionUser, String sessionType);

    SessionVO querySessionById(Long id);

    void insertSession(String sessionId, String sessionUser, String sessionTitle, String sessionType);

    void updateSessionTitle(Long id, String sessionTitle);

    void deleteSession(Long id);

    void deleteMessagesBySessionId(String sessionId);

    List<MessageVO> listMessageBySessionAndType(String sessionId, String messageType);

    SessionVO querySessionBySessionId(String sessionId);
}
