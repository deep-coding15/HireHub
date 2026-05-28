package com.hirehub.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Génère un X-Correlation-Id unique pour chaque requête entrante et le propage
 * dans le header HTTP vers tous les services avals.
 *
 * Pourquoi ici : le Gateway est le seul point d'entrée — c'est l'endroit idéal
 * pour créer l'ID une seule fois et le distribuer à toute la chaîne.
 *
 * Si le client envoie déjà un X-Correlation-Id (ex : Postman, frontend), il est conservé.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalId = correlationId;

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER, finalId)
                .build();

        // Renvoyer l'ID dans la réponse — utile pour le debugging côté client
        exchange.getResponse().getHeaders().add(HEADER, finalId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // Highest precedence = s'exécute avant tous les autres filtres Gateway
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
