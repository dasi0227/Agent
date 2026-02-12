package com.dasi.type.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "bocha")
@Component
@Data
public class BochaProperties {

    private String baseUrl;

    private String apiKey;

}
