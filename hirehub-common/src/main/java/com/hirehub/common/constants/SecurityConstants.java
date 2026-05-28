package com.hirehub.common.constants;

public class SecurityConstants {
    private SecurityConstants() {}

    // Headers injectés par la Gateway dans chaque requête
    public static final String HEADER_USER_ID   = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_NAME = "X-User-Name";

    // Préfixe JWT
    public static final String BEARER_PREFIX = "Bearer ";

    // Header de corrélation — généré par l'API Gateway, propagé dans toute la chaîne
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
}
