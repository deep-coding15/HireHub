package com.hirehub.frontend.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Liste des headers potentiellement utilisee par l'équipe Auth
                String[] authHeaders = {
                        "Authorization",
                        "X-User-Id",
                        "X-User-Email",
                        "X-User-Role",
                        "X-User-Name",
                        "Cookie",
                        "X-Correlation-Id"
                };
                // 1. Essayer de propager le header Authorization (si présent)
                // On propage le token JWT vers le microservice suivant
                for (String header : authHeaders) {
                    String value = request.getHeader(header);
                    if (value != null) {
                        requestTemplate.header(header, value);
                    }
                }

                // 2. copier aussi les Cookies (essentiel pour JSESSIONID)
                String cookieHeader = request.getHeader("Cookie");
                if (cookieHeader != null) {
                    requestTemplate.header("Cookie", cookieHeader);
                }
            }

            // Alternative si le token est dans le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() != null) {
                String token = authentication.getCredentials().toString();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
            if (authentication != null && authentication.getPrincipal() != null) {
                // Cas OAuth2 / Google : on cherche l'IdToken ou l'AccessToken
                if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
                    // Si tu as accès à OAuth2AuthorizedClientService, c'est encore mieux,
                    // mais testons déjà avec les credentials si disponibles :
                    if (authentication.getCredentials() != null) {
                        requestTemplate.header("Authorization", "Bearer " + authentication.getCredentials().toString());
                    }
                }
                // Cas Login Formulaire classique (JWT stocké dans les détails ou credentials)
                else if (authentication.getCredentials() != null) {
                    String token = authentication.getCredentials().toString();
                    if (!token.isEmpty()) {
                        requestTemplate.header("Authorization", "Bearer " + token);
                    }
                }
            }
        };
    }
}
