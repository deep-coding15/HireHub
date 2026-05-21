package com.hirehub.entretien.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI entretienOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Entretien Service API")
                        .description("Gestion des entretiens — HireHub")
                        .version("1.0.0"));
    }
}