package com.dasi.trigger.http;

import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.session.service.ISessionService;
import com.dasi.types.dto.request.session.InsertSessionRequest;
import com.dasi.types.dto.request.session.UpdateSessionRequest;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/session")
public class SessionController {

    @Resource
    private ISessionService sessionSevice;

    // 根据 userId 拿，userId 在 JWT
    @GetMapping("/list")
    public Result<List<SessionVO>> listSession() {
        return Result.success(sessionSevice.listSession());
    }

    // 每个类型的上限是三个，需要抛出 SessionException 来在前端提示
    @PostMapping("/insert")
    public Result<Void> insertSession(@Valid @RequestBody InsertSessionRequest request) {
        sessionSevice.insertSession(request);
        return Result.success();
    }

    // 只允许更新标题
    @PostMapping("/update")
    public Result<Void> updateSession(@Valid @RequestBody UpdateSessionRequest request) {
        sessionSevice.updateSession(request);
        return Result.success();
    }

    // 需要同步删除 message 表的数据
    @PostMapping("/delete")
    public Result<Void> deleteSession(@RequestParam("id") Long id) {
        sessionSevice.deleteSession(id);
        return Result.success();
    }

    // 获取 chat 类型的数据
    @GetMapping("/message/chat")
    public Result<List<MessageVO>> listChatMessage() {
        return Result.success(sessionSevice.listChatMessage());
    }

    // 获取 work-sse 类型的数据
    @GetMapping("/message/work-sse")
    public Result<List<MessageVO>> listWorkSseMessage() {
        return Result.success(sessionSevice.listWorkSseMessage());
    }

    // 获取 work-answer 类型的数据
    @GetMapping("/message/work-answer")
    public Result<List<MessageVO>> listWorkAnswerSession() {
        return Result.success(sessionSevice.listWorkAnswerSession());
    }

}
