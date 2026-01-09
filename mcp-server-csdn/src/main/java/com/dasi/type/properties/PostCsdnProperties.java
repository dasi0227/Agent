package com.dasi.type.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "csdn")
@Data
@Component
public class PostCsdnProperties {

    private String baseUrl;

    private String cookie;

    private String categories;

    private String tags;

    private String coverUrl;
}
