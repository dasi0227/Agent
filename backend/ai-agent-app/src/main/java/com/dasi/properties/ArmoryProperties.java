package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "armory",  ignoreInvalidFields = true)
public class ArmoryProperties {

    private Boolean enable;

    private Set<String> clientIdList;

    private Set<String> agentIdList;

}
