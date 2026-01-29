package com.dasi.domain.ai.service.execute;

import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IExecuteStrategy {

    void execute(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) throws Exception;

    String getType();

}
