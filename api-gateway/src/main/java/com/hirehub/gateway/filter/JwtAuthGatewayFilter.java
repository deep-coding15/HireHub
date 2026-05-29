package com.hirehub.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtre global du gateway qui valide le token JWT Bearer si présent et injecte
 * les headers X-User-* dans la requête routée vers les microservices.
 *
 * Logique :
 *  - Pas d'Authorization header → la requête passe telle quelle (le service en
 *    aval décide si l'auth est obligatoire via ses propres checks).
 *  - Authorization: Bearer <token> présent ET valide → les headers X-User-* sont
 *    injectés/écrasés à partir des claims du JWT.
 *  - Authorization: Bearer <token> présent ET invalide (expiré, mauvaise signature)
 *    → 401 Unauthorized renvoyé immédiatement.
 */
@Component
@Slf4j
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    // Noms des headers injectés (alignés sur SecurityConstants du module common)
    private static final String HEADER_USER_ID    = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE  = "X-User-Role";
    private static final String HEADER_USER_NAME  = "X-User-Name";

    // Claims JWT (alignés sur JwtClaimNames du module common)
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE  = "role";

    private final ReactiveJwtDecoder jwtDecoder;

    public JwtAuthGatewayFilter(ReactiveJwtDecoder hirehubReactiveJwtDecoder) {
        this.jwtDecoder = hirehubReactiveJwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Pas de token → on laisse passer, le service en aval gère l'absence d'auth
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    String userId = jwt.getSubject();
                    String email  = jwt.getClaimAsString(CLAIM_EMAIL);
                    String role   = jwt.getClaimAsString(CLAIM_ROLE);

                    log.debug("JWT valide pour userId={} role={}", userId, role);

                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r
                                    .header(HEADER_USER_ID,    userId != null ? userId : "")
                                    .header(HEADER_USER_EMAIL, email  != null ? email  : "")
                                    .header(HEADER_USER_ROLE,  role   != null ? role   : "")
                                    .header(HEADER_USER_NAME,  email  != null ? email  : ""))
                            .build();
                    return chain.filter(mutated);
                })
                .onErrorResume(e -> {
                    if (e instanceof JwtException) {
                        log.warn("Token JWT invalide rejeté par le gateway : {}", e.getMessage());
                    } else {
                        log.error("Erreur inattendue lors de la validation JWT", e);
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        // Après CorrelationIdFilter (HIGHEST_PRECEDENCE) mais avant le routing
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
