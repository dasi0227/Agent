package com.dasi.trigger.http;

import com.dasi.api.ISessionApi;
import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.session.service.ISessionService;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/session")
public class SessionController implements ISessionApi {

    @Resource
    private ISessionService sessionSevice;

    @GetMapping("/list")
    @Override
    public Result<List<SessionVO>> listSession() {
        return Result.success(sessionSevice.listSession());
    }

    @PostMapping("/insert")
    @Override
    public Result<SessionVO> insertSession(@RequestParam("sessionTitle") String sessionTitle, @RequestParam("sessionType") String sessionType) {
        return Result.success(sessionSevice.insertSession(sessionTitle, sessionType));
    }

    @PostMapping("/update")
    @Override
    public Result<Void> updateSession(@RequestParam("id") Long id, @RequestParam("sessionTitle") String sessionTitle) {
        sessionSevice.updateSession(id, sessionTitle);
        return Result.success();
    }

    @PostMapping("/delete")
    @Override
    public Result<Void> deleteSession(@RequestParam("id") Long id, @RequestParam("sessionId") String sessionId) {
        sessionSevice.deleteSession(id, sessionId);
        return Result.success();
    }

    @GetMapping("/message/chat")
    @Override
    public Result<List<MessageVO>> listChatMessage(@RequestParam("sessionId") String sessionId) {
        return Result.success(sessionSevice.listChatMessage(sessionId));
    }

    @GetMapping("/message/work-sse")
    @Override
    public Result<List<MessageVO>> listWorkSseMessage(@RequestParam("sessionId") String sessionId) {
        return Result.success(sessionSevice.listWorkSseMessage(sessionId));
    }

    @GetMapping("/message/work-answer")
    @Override
    public Result<List<MessageVO>> listWorkAnswerSession(@RequestParam("sessionId") String sessionId) {
        return Result.success(sessionSevice.listWorkAnswerMessage(sessionId));
    }

}
