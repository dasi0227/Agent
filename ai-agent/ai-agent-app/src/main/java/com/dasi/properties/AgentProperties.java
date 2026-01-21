package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "agent",  ignoreInvalidFields = true)
public class AgentProperties {

    private List<String> initClientIdList;

}
