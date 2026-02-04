package com.dasi.domain.session.service;

import com.dasi.domain.session.model.enumeration.UserRoleType;
import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.session.repository.ISessionRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.types.exception.SessionException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.dasi.domain.session.model.enumeration.MessageType.WORK_ANSWER;
import static com.dasi.domain.session.model.enumeration.MessageType.WORK_SSE;
import static com.dasi.domain.session.model.enumeration.SessionType.CHAT;
import static com.dasi.types.constant.ChatConstant.CHAT_SESSION_LIMIT;

@Slf4j
@Service
public class SessionService implements ISessionService {

    @Resource
    private ISessionRepository sessionRepository;

    @Resource
    private AuthContext authContext;

    @Override
    public List<SessionVO> listSession() {
        return sessionRepository.listSession(authContext.getUsername());
    }

    @Override
    public SessionVO insertSession(String sessionTitle, String sessionType) {
        String sessionUser = authContext.getUsername();

        int count = sessionRepository.countSessionByType(sessionUser, sessionType);
        if (count >= CHAT_SESSION_LIMIT) {
            throw new SessionException("每种类型最多 3 个会话");
        }

        String sessionId = generateSessionId(sessionType);
        sessionRepository.insertSession(sessionId, sessionUser, sessionTitle, sessionType);
        return sessionRepository.querySessionBySessionId(sessionId);
    }

    @Override
    public void updateSession(Long id, String sessionTitle) {
        SessionVO sessionVO = requireSession(id);
        String sessionUser = requireUser();
        if (!sessionUser.equals(sessionVO.getSessionUser())) {
            throw new SessionException("无权限修改该会话");
        }

        String title = StringUtils.hasText(sessionTitle) ? sessionTitle.trim() : "未命名会话";
        sessionRepository.updateSessionTitle(id, title);
    }

    @Override
    public void deleteSession(Long id, String sessionId) {
        SessionVO sessionVO = requireSession(sessionId);
        String sessionUser = requireUser();
        if (!sessionUser.equals(sessionVO.getSessionUser())) {
            throw new SessionException("无权限修改该会话");
        }

        sessionRepository.deleteMessagesBySessionId(sessionId);
        sessionRepository.deleteSession(id);
    }

    @Override
    public List<MessageVO> listChatMessage(String sessionId) {
        SessionVO sessionVO = requireSession(sessionId);
        String sessionUser = requireUser();
        if (notAdmin() && !sessionUser.equals(sessionVO.getSessionUser())) {
            throw new SessionException("无权限修改该会话");
        }

        return sessionRepository.listMessageBySessionAndType(sessionId, CHAT.getType());
    }

    @Override
    public List<MessageVO> listWorkSseMessage(String sessionId) {
        SessionVO sessionVO = requireSession(sessionId);
        String sessionUser = requireUser();
        if (notAdmin() && !sessionUser.equals(sessionVO.getSessionUser())) {
            throw new SessionException("无权限修改该会话");
        }

        return sessionRepository.listMessageBySessionAndType(sessionId, WORK_SSE.getType());
    }

    @Override
    public List<MessageVO> listWorkAnswerMessage(String sessionId) {
        SessionVO sessionVO = requireSession(sessionId);
        String sessionUser = requireUser();
        if (notAdmin() && !sessionUser.equals(sessionVO.getSessionUser())) {
            throw new SessionException("无权限修改该会话");
        }

        return sessionRepository.listMessageBySessionAndType(sessionId, WORK_ANSWER.getType());
    }

    private boolean notAdmin() {
        String role = authContext.getRole();
        if (!StringUtils.hasText(role)) {
            throw new SessionException("用户信息缺失");
        }
        return !role.equals(UserRoleType.ADMIN.getType());
    }


    private SessionVO requireSession(Long id) {
        SessionVO session = sessionRepository.querySessionById(id);
        if (session == null) {
            throw new SessionException("会话不存在");
        }
        return session;
    }

    private SessionVO requireSession(String sessionId) {
        SessionVO session = sessionRepository.querySessionBySessionId(sessionId);
        if (session == null) {
            throw new SessionException("会话不存在");
        }
        return session;
    }

    private String requireUser() {
        String username = authContext.getUsername();
        if (!StringUtils.hasText(username)) {
            throw new SessionException("用户信息缺失");
        }
        return username;
    }

    private String generateSessionId(String type) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return "%s_%s_%s".formatted(date, type, uuid);
    }

}
