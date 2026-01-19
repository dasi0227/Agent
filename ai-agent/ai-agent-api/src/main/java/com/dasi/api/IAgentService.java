package com.dasi.api;

import com.dasi.api.dto.AgentAutoRequestDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface IAgentService {

    ResponseBodyEmitter agentAuto(AgentAutoRequestDTO agentAutoRequestDTO, HttpServletResponse response);

}
