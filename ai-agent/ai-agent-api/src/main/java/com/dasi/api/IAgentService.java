package com.dasi.api;

import com.dasi.types.dto.request.ArmoryRequest;
import com.dasi.types.dto.request.ExecuteRequest;
import com.dasi.types.dto.result.Result;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IAgentService {

    SseEmitter execute(ExecuteRequest executeRequest);

    Result<Void> armory(ArmoryRequest armoryRequest);

}
