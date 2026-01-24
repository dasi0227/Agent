package com.dasi.trigger.http;

import com.dasi.api.IAgentService;
import com.dasi.api.dto.ArmoryRequestDTO;
import com.dasi.api.dto.ExecuteRequestDTO;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.dispatch.IDispatchService;
import com.dasi.types.model.Result;
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
    public Result<Void> armory(@Valid @RequestBody ArmoryRequestDTO armoryRequestDTO) {

        String armoryType = armoryRequestDTO.getArmoryType();
        List<String> idList = armoryRequestDTO.getIdList();

        dispatchService.dispatchArmoryStrategy(armoryType, idList);

        return Result.success();
    }

    @Override
    @PostMapping(value = "/execute", produces = "text/event-stream")
    public SseEmitter execute(@Valid @RequestBody ExecuteRequestDTO executeRequestDTO) {

        SseEmitter sseEmitter = new SseEmitter(0L);

        ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                .aiAgentId(executeRequestDTO.getAiAgentId())
                .userMessage(executeRequestDTO.getUserMessage())
                .sessionId(executeRequestDTO.getSessionId())
                .maxRound(executeRequestDTO.getMaxRound())
                .maxRetry(executeRequestDTO.getMaxRetry())
                .build();

        dispatchService.dispatchExecuteStrategy(executeRequestEntity, sseEmitter);

        return sseEmitter;
    }

}
