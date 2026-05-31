package com.loltracker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class RiotApiConfig {

    @Value("${riot.api.base-url}")
    private String baseUrl;

    @Value("${riot.api.key}")
    private String apiKey;

    @Bean
    public WebClient riotWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }
}
