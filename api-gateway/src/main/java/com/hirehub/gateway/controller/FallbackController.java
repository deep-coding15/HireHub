package com.hirehub.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Réponses de repli retournées par le Gateway quand un circuit breaker est OPEN.
 * Chaque endpoint correspond à un service backend surveillé.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/offre-service")
    @PostMapping("/offre-service")
    public ResponseEntity<Map<String, String>> offreFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "unavailable",
                        "message", "Le service des offres est temporairement indisponible. Veuillez réessayer dans quelques instants.",
                        "retryAfter", "30"
                ));
    }

    @GetMapping("/candidature-service")
    @PostMapping("/candidature-service")
    public ResponseEntity<Map<String, String>> candidatureFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "unavailable",
                        "message", "Le service des candidatures est temporairement indisponible. Veuillez réessayer dans quelques instants.",
                        "retryAfter", "30"
                ));
    }

    @GetMapping("/auth-service")
    @PostMapping("/auth-service")
    public ResponseEntity<Map<String, String>> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "unavailable",
                        "message", "Le service d'authentification est temporairement indisponible. Veuillez réessayer dans quelques instants.",
                        "retryAfter", "30"
                ));
    }

    @GetMapping("/entretien-service")
    @PostMapping("/entretien-service")
    public ResponseEntity<Map<String, String>> entretienFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "unavailable",
                        "message", "Le service des entretiens est temporairement indisponible. Veuillez réessayer dans quelques instants.",
                        "retryAfter", "30"
                ));
    }
}
