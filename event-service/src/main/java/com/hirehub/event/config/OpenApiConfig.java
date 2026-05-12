package com.hirehub.event.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hireHubEventOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireHub — Event Service")
                        .version("1.0")
                        .description("""
                                Journal d'audit des événements (`/api/event-logs`). \
                                Un en-tête `Authorization: Bearer <JWT>` valide enrichit le contexte utilisateur.
                                """));
    }
}
