package com.dasi.domain.login.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt", ignoreInvalidFields = true)
public class JwtProperties {

    private String secret = "dasi-agent-secret";

    private Long expireSeconds = 86400L;

    private String issuer = "ai-agent";
}
