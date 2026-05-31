package com.loltracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LoL Tierpeek API")
                        .description("League of Legends 친구 랭크 추적 백엔드 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LoL Tierpeek")
                                .url("https://github.com/minky5004/LOL_tierpeek")));
    }
}
