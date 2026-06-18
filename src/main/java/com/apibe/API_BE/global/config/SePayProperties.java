package com.apibe.API_BE.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sepay")
@Getter
@Setter
public class SePayProperties {
    private String apiKey;
    private String bankId;
    private String accountNumber;
    private String accountName;
}
