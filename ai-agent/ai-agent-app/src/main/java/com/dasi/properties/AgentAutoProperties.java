package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "agent.auto",  ignoreInvalidFields = true)
public class AgentAutoProperties {

    private boolean enabled;

    private List<String> clientIdList;

}
