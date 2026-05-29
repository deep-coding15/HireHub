package com.hirehub.candidature.config;

import org.springframework.context.annotation.Configuration;

/**
 * La validation JWT a été déplacée vers l'API Gateway (modèle gateway trust).
 * Ce service lit les identités depuis les headers X-User-* via UserContextHeaderFilter.
 */
@Configuration
public class CandidatureJwtConfig {
    // Vide intentionnellement — voir JwtAuthGatewayFilter dans api-gateway
}
