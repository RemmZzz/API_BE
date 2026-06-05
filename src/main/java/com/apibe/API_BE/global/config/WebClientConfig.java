package com.apibe.API_BE.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WebClientConfig {

    /**
     * Shared RestClient.Builder bean used by ApiTesterService.
     * We expose the builder (not a built instance) so the service can configure
     * per-request timeouts via a fresh SimpleClientHttpRequestFactory each call.
     *
     * Spring Boot 3.2+ (Spring 6.1+): RestClient is the recommended
     * synchronous HTTP client replacing RestTemplate.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
