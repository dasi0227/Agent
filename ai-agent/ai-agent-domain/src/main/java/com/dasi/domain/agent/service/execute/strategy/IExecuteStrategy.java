package com.dasi.domain.agent.service.execute.strategy;

import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface IExecuteStrategy {

    void execute(ExecuteRequestEntity executeRequestEntity, ResponseBodyEmitter emitter) throws Exception;

}
