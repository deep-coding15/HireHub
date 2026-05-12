package com.hirehub.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hireHubGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireHub — API Gateway")
                        .version("1.0")
                        .description("""
                                Passerelle Spring Cloud Gateway (WebFlux). \
                                Les routes dynamiques Eureka sont ici ; pour documenter chaque microservice, \
                                ouvrez son propre Swagger sur son port (voir collection Postman).
                                """));
    }
}
