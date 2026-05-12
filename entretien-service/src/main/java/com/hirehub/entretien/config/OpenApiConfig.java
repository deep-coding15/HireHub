package com.hirehub.entretien.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hireHubEntretienOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireHub — Entretien Service")
                        .version("1.0")
                        .description("Planification et suivi des entretiens (endpoints REST à étendre selon le module)."));
    }
}
