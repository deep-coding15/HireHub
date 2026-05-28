package com.hirehub.common.logging;

import com.hirehub.common.constants.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtre HTTP qui injecte le correlationId dans le MDC SLF4J.
 *
 * Pourquoi : le MDC est automatiquement ajouté à chaque log produit par le thread courant.
 * Sans ce filtre, il est impossible de relier les logs d'une même requête entre services.
 *
 * Enregistré automatiquement via LoggingAutoConfiguration (Spring Boot auto-config).
 * Actif uniquement sur les services Servlet (pas sur api-gateway qui est WebFlux).
 */
@Order(1)
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String correlationId = request.getHeader(SecurityConstants.HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            // Appel direct hors Gateway (dev local, tests) : on génère un ID local
            correlationId = UUID.randomUUID().toString();
        }

        try {
            MDC.put("correlationId", correlationId);

            // Les headers X-User-Id et X-User-Email sont injectés par la Gateway
            // après validation du JWT, et propagés par les FeignClientConfig
            String userId = request.getHeader(SecurityConstants.HEADER_USER_ID);
            String userEmail = request.getHeader(SecurityConstants.HEADER_USER_EMAIL);
            if (userId != null && !userId.isBlank()) {
                MDC.put("userId", userId);
            }
            if (userEmail != null && !userEmail.isBlank()) {
                MDC.put("userEmail", userEmail);
            }

            response.setHeader(SecurityConstants.HEADER_CORRELATION_ID, correlationId);
            filterChain.doFilter(request, response);

        } finally {
            // OBLIGATOIRE : les threads Tomcat sont réutilisés (pool).
            // Sans clear(), le MDC de la requête N contamine la requête N+1.
            MDC.clear();
        }
    }
}
