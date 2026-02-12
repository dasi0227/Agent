package com.dasi.sse.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "email")
@Component
@Data
public class EmailProperties {

    private String fromAddress;

    private String fromName;

}
