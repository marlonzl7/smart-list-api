package com.smartlist.api.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "password.reset.rate-limit")
public class RateLimitProperties {
    private int email;
    private int ip;
    private int duration;
}
