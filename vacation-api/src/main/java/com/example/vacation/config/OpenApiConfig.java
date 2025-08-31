package com.example.vacation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vacationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("Vacation Management API")
                    .description("API for managing employee vacation requests")
                    .version("1.0"));
    }
}
