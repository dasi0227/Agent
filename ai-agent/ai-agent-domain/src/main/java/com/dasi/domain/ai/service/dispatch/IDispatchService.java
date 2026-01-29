package com.dasi.domain.ai.service.dispatch;

import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

public interface IDispatchService {

    void dispatchArmoryStrategy(String armoryType, Set<String> idSet);

    void dispatchExecuteStrategy(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter);

}
