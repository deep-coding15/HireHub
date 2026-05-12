package com.hirehub.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hireHubAuthOpenAPI() {
        final String schemeName = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("HireHub — Auth Service")
                        .version("1.0")
                        .description("""
                                Inscription et authentification. Jeton JWT HS256 via `POST /api/v1/auth/login`.
                                Claims : `sub`, `email`, `role`, `recruteurApprouve` (voir `docs/API-CONTRACTS.md`).
                                """))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Valeur du champ `accessToken` renvoyé par le login, préfixée par `Bearer ` dans l'en-tête `Authorization`.")
                ));
    }
}
