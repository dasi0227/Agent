package com.dasi.domain.agent.service.execute;

import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IExecuteStrategy {

    void execute(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) throws Exception;

}
