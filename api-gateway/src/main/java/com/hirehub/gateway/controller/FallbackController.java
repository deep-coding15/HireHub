package com.hirehub.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Réponses de repli retournées par le Gateway quand un circuit breaker est OPEN.
 * @RequestMapping sans méthode = répond à tous les verbes HTTP (GET, POST, PUT, DELETE, PATCH).
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/offre-service")
    public ResponseEntity<Map<String, String>> offreFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "unavailable",
                        "message", "Le service des offres est temporairement indisponible. Veuillez réessayer dans quelques instants.",
                        "retryAfter", "30"
                ));
    }

    @RequestMapping("/candidature-service")
    public ResponseEntity<Map<String, String>> candidatureFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "unavailable",
                        "message", "Le service des candidatures est temporairement indisponible. Veuillez réessayer dans quelques instants.",
                        "retryAfter", "30"
                ));
    }

    @RequestMapping("/auth-service")
    public ResponseEntity<Map<String, String>> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "unavailable",
                        "message", "Le service d'authentification est temporairement indisponible. Veuillez réessayer dans quelques instants.",
                        "retryAfter", "30"
                ));
    }

    @RequestMapping("/entretien-service")
    public ResponseEntity<Map<String, String>> entretienFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "unavailable",
                        "message", "Le service des entretiens est temporairement indisponible. Veuillez réessayer dans quelques instants.",
                        "retryAfter", "30"
                ));
    }
}
