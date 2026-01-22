package com.dasi.api;

import com.dasi.api.dto.ArmoryRequestDTO;
import com.dasi.api.dto.ExecuteRequestDTO;
import com.dasi.types.model.Result;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IAgentService {

    SseEmitter execute(ExecuteRequestDTO executeRequestDTO);

    Result<Void> armory(ArmoryRequestDTO armoryRequestDTO);

}
