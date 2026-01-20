package com.dasi.api;

import com.dasi.api.dto.AgentAutoRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IAgentService {

    SseEmitter agentAuto(AgentAutoRequestDTO agentAutoRequestDTO);

}
