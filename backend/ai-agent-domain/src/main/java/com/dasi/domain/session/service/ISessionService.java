package com.dasi.domain.session.service;

import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;

import java.util.List;

public interface ISessionService {

    List<SessionVO> listSession();

    SessionVO insertSession(String sessionTitle, String sessionType);

    void updateSession(Long id, String sessionTitle);

    void deleteSession(Long id, String sessionId);

    List<MessageVO> listChatMessage(String sessionId);

    List<MessageVO> listWorkSseMessage(String sessionId);

    List<MessageVO> listWorkAnswerMessage(String sessionId);
}
