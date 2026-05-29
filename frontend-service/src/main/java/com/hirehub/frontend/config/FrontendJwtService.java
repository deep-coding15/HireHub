package com.hirehub.frontend.config;

/**
 * Remplacé par l'injection directe des headers X-User-* dans FeignClientConfig.
 * La validation JWT est désormais centralisée dans l'api-gateway (Option A).
 */
public class FrontendJwtService {
    // Vide intentionnellement — voir FeignClientConfig et JwtAuthGatewayFilter
}
