package com.hirehub.candidature.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter implements Filter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/actuator/health"
    );

    private final JwtTokenValidator jwtTokenValidator;

    public JwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Guard 1 : contexte non-HTTP (rare mais défensif)
        if (!(request instanceof HttpServletRequest httpRequest) ||
                !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // Guard 2 : routes publiques — pas besoin de token
            if (isPublicPath(httpRequest)) {
                log.debug("[JWT] Route publique, filtre ignoré : {}", httpRequest.getRequestURI());
                chain.doFilter(request, response);
                return;
            }

            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                processToken(token, httpResponse);

                // Si la réponse a déjà été envoyée (401), on ne continue pas
                if (httpResponse.isCommitted()) {
                    return;
                }
            } else {
                log.debug("[JWT] Aucun token dans Authorization — URI={}", httpRequest.getRequestURI());
                // Pas de token sur une route protégée = 401
                sendUnauthorized(httpResponse, "Token manquant");
                return;
            }

            chain.doFilter(request, response);

        } finally {
            // Toujours nettoyer le ThreadLocal, même en cas d'exception
            UserContext.clear();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers privés
    // -------------------------------------------------------------------------

    /**
     * Valide le token et hydrate le UserContext.
     * En cas d'échec, envoie une 401 et ne touche pas au contexte.
     */
    private void processToken(String token, HttpServletResponse httpResponse) throws IOException {
        try {
            if (!jwtTokenValidator.isTokenValid(token)) {
                log.warn("[JWT] Token invalide ou expiré");
                sendUnauthorized(httpResponse, "Token invalide ou expiré");
                return;
            }

            // Un seul bloc de parsing — même si extractX() parse,
            // on garde la lisibilité en attendant un extractAllClaims()
            UUID userId   = jwtTokenValidator.extractUserId(token);
            String email  = jwtTokenValidator.extractEmail(token);
            String role   = jwtTokenValidator.extractRole(token);
            String fullName = jwtTokenValidator.extractFullName(token);

            UserContext.setUser(userId, email, role, fullName);

            // PII masqué en prod : on ne logue que userId
            log.debug("[JWT] Authentification réussie — userId={}", userId);

        } catch (ExpiredJwtException e) {
            log.warn("[JWT] Token expiré — {}", e.getMessage());
            sendUnauthorized(httpResponse, "Token expiré");

        } catch (MalformedJwtException e) {
            log.warn("[JWT] Token malformé ou signature invalide — {}", e.getMessage());
            sendUnauthorized(httpResponse, "Token invalide");

        } catch (JwtException e) {
            // Catch-all JWT : autres cas (UnsupportedJwt, etc.)
            log.warn("[JWT] Erreur JWT inattendue — {}", e.getMessage());
            sendUnauthorized(httpResponse, "Token invalide");
        }
    }

    /**
     * Envoie une réponse 401 avec un corps JSON minimal.
     * Préférable à sendError() qui peut déclencher une page d'erreur du conteneur.
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (response.isCommitted()) {
            log.warn("[JWT] Réponse déjà committée, impossible d'envoyer 401");
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"status": 401, "error": "Unauthorized", "message": "%s"}
                """.formatted(message));
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(uri::startsWith);
    }
}