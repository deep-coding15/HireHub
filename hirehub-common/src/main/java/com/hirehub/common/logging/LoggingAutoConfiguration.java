package com.hirehub.common.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration Spring Boot pour le filtre MDC.
 *
 * Pourquoi auto-configuration et pas @Component :
 * - @Component nécessite que le package com.hirehub.common soit scanné par chaque service.
 * - L'auto-configuration est chargée par Spring Boot automatiquement pour toute app
 *   qui a hirehub-common sur son classpath, sans modifier les @ComponentScan existants.
 *
 * @ConditionalOnWebApplication(SERVLET) : le filtre ne s'applique qu'aux services MVC.
 * L'api-gateway (WebFlux) est automatiquement exclu.
 *
 * Déclarée dans META-INF/spring/org.springframework.boot.autoconfigure.EnableAutoConfiguration
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LoggingAutoConfiguration {

    @Bean
    public MdcLoggingFilter mdcLoggingFilter() {
        return new MdcLoggingFilter();
    }
}
