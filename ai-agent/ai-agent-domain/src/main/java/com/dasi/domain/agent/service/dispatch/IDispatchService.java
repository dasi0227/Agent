package com.dasi.domain.agent.service.dispatch;

import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface IDispatchService {

    void dispatchArmoryStrategy(String armoryType, List<String> idList);

    void dispatchExecuteStrategy(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter);

}
