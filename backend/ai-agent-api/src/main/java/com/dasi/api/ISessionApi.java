package com.dasi.api;

import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface ISessionApi {

    Result<List<SessionVO>> listSession();

    Result<SessionVO> insertSession(String sessionTitle, String sessionType);

    Result<Void> updateSession(Long id, String sessionTitle);

    Result<Void> deleteSession(Long id, String sessionId);

    Result<List<MessageVO>> listChatMessage(String sessionId);

    Result<List<MessageVO>> listWorkSseMessage(String sessionId);

    Result<List<MessageVO>> listWorkAnswerSession(String sessionId);
}
