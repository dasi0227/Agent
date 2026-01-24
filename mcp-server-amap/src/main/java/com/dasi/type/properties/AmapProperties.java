package com.dasi.type.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "amap")
@Component
@Data
public class AmapProperties {

    private String baseUrl;

    private String apiKey;

}
