package com.tierpeek.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RiotApiConfig {

    @Value("${riot.api.key}")
    private String apiKey;

    @Bean
    public WebClient.Builder riotWebClientBuilder() {
        return WebClient.builder()
                .defaultHeader("X-Riot-Token", apiKey)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                        .build());
    }
}
