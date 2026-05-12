package com.hirehub.email.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hireHubEmailOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireHub — Email / Notification Service")
                        .version("1.0")
                        .description("Envoi d'e-mails déclenché par des appels HTTP internes (confirmations candidature, statuts, entretiens)."));
    }
}
