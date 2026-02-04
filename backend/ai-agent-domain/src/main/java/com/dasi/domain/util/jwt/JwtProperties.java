package com.dasi.domain.util.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt", ignoreInvalidFields = true)
public class JwtProperties {

    private String secret;

    private Long expireSeconds;

    private String issuer;

}
