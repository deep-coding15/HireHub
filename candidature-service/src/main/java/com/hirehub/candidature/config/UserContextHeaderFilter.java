package com.hirehub.candidature.config;

import com.hirehub.common.constants.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Lit les headers X-User-* injectés par l'API Gateway après validation du JWT
 * et les place dans {@link UserContext} pour la durée de la requête.
 *
 * Remplace {@link JwtAuthenticationFilter} : le service ne valide plus de JWT,
 * il fait confiance aux headers injectés par le gateway (modèle "gateway trust").
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class UserContextHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userId = request.getHeader(SecurityConstants.HEADER_USER_ID);
            String email  = request.getHeader(SecurityConstants.HEADER_USER_EMAIL);
            String role   = request.getHeader(SecurityConstants.HEADER_USER_ROLE);
            String name   = request.getHeader(SecurityConstants.HEADER_USER_NAME);

            if (userId != null && !userId.isBlank()) {
                try {
                    UUID uuid = UUID.fromString(userId);
                    UserContext.setUser(new UserContext.UserInfo(
                            uuid, email, name != null ? name : email,
                            role, name, null, null, null, true, false));
                    log.debug("UserContext initialisé : userId={} role={}", userId, role);
                } catch (IllegalArgumentException e) {
                    log.warn("Header X-User-Id invalide (pas un UUID) : {}", userId);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
