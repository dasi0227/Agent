package com.dasi.type.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "wecom")
@Component
@Data
public class WeComProperties {

    private String baseUrl;

    private Integer agentid;

    private String corpid;

    private String corpsecret;

}
