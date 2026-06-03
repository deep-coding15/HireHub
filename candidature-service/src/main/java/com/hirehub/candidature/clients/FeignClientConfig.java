package com.hirehub.candidature.clients;

import com.hirehub.candidature.config.UserContext;
import com.hirehub.common.constants.SecurityConstants;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Propage les headers X-User-* et X-Correlation-Id dans les appels Feign sortants.
 * L'identité est lue depuis UserContext (peuplé par UserContextHeaderFilter).
 * Plus de JWT Bearer : le service fait confiance au modèle gateway-trust.
 */
@Configuration
public class FeignClientConfig {

    @Bean
    @Profile("!sandbox")
    public IOffreServiceClientFallback iOffreServiceClientFallback() {
        return new IOffreServiceClientFallback();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Corrélation distribuée
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                requestTemplate.header(SecurityConstants.HEADER_CORRELATION_ID, correlationId);
            }

            // Propagation de l'identité depuis le UserContext courant
            UserContext.UserInfo user = UserContext.getUser();
            if (user != null) {
                if (user.userId != null) {
                    requestTemplate.header(SecurityConstants.HEADER_USER_ID, user.userId.toString());
                }
                if (user.email != null && !user.email.isBlank()) {
                    requestTemplate.header(SecurityConstants.HEADER_USER_EMAIL, user.email);
                }
                if (user.username != null && !user.username.isBlank()) {
                    requestTemplate.header(SecurityConstants.HEADER_USER_NAME, user.username);
                }
                if (user.role != null && !user.role.isBlank()) {
                    requestTemplate.header(SecurityConstants.HEADER_USER_ROLE, user.role);
                }
            }
        };
    }
}
