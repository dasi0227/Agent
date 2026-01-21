package com.dasi.api;

import com.dasi.api.dto.AgentRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IAgentService {

    SseEmitter agent(AgentRequestDTO agentRequestDTO);

}
