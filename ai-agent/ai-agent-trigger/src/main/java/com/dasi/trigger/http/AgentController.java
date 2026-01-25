package com.dasi.trigger.http;

import com.dasi.api.IAgentService;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.dispatch.IDispatchService;
import com.dasi.types.dto.request.ArmoryRequest;
import com.dasi.types.dto.request.ExecuteRequest;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController implements IAgentService {

    @Resource
    private IDispatchService dispatchService;

    @Override
    @PostMapping(value = "/armory")
    public Result<Void> armory(@Valid @RequestBody ArmoryRequest armoryRequest) {

        String armoryType = armoryRequest.getArmoryType();
        List<String> idList = armoryRequest.getIdList();

        dispatchService.dispatchArmoryStrategy(armoryType, idList);

        return Result.success();
    }

    @Override
    @PostMapping(value = "/execute", produces = "text/event-stream")
    public SseEmitter execute(@Valid @RequestBody ExecuteRequest executeRequest) {

        SseEmitter sseEmitter = new SseEmitter(0L);

        ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                .aiAgentId(executeRequest.getAiAgentId())
                .userMessage(executeRequest.getUserMessage())
                .sessionId(executeRequest.getSessionId())
                .maxRound(executeRequest.getMaxRound())
                .maxRetry(executeRequest.getMaxRetry())
                .build();

        dispatchService.dispatchExecuteStrategy(executeRequestEntity, sseEmitter);

        return sseEmitter;
    }

}
