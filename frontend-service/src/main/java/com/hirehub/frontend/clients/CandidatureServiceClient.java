package com.hirehub.frontend.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Client Feign pour communiquer avec candidature-service
 * Gère toutes les opérations liées aux candidatures
 */
@FeignClient(name = "candidature-service")
public interface CandidatureServiceClient {

    /**
     * Récupère toutes les candidatures du candidat authentifié
     * GET /candidatures/moi
     */
    @GetMapping("/candidatures/moi")
    List<CandidatureDTO> getMyCandidatures();

    /**
     * Récupère une candidature spécifique par ID
     * GET /candidatures/{id}
     */
    @GetMapping("/candidatures/{id}")
    CandidatureDTO getCandidature(@PathVariable("id") String id);

    /**
     * Crée une nouvelle candidature
     * POST /candidatures
     */
    @PostMapping("/candidatures")
    CandidatureDTO createCandidature(@RequestBody CandidatureDTO dto);

    /**
     * Met à jour le statut d'une candidature
     * PUT /candidatures/{id}/status?status=ACCEPTEE
     */
    @PutMapping("/candidatures/{id}/status")
    void updateStatus(
        @PathVariable("id") String id,
        @RequestParam("status") String status
    );

    /**
     * Supprime une candidature
     * DELETE /candidatures/{id}
     */
    @DeleteMapping("/candidatures/{id}")
    void deleteCandidature(@PathVariable("id") String id);

    /**
     * Récupère l'historique des changements de statut
     * GET /candidatures/{id}/historique
     */
    @GetMapping("/candidatures/{id}/historique")
    List<HistoriqueStatutDTO> getHistorique(@PathVariable("id") String id);

    /**
     * Récupère les candidatures pour une offre (pipeline recruteur)
     * GET /candidatures/offre/{offreId}
     */
    @GetMapping("/candidatures/offre/{offreId}")
    List<CandidatureDTO> getCandidaturesByOffre(@PathVariable("offreId") String offreId);

    /**
     * Télécharge les fichiers (CV ou lettre) d'une candidature
     * GET /candidatures/{id}/download?type=cv|lettre
     */
    @GetMapping("/candidatures/{id}/download")
    ResponseEntity<String> downloadFile(
        @PathVariable("id") String id,
        @RequestParam(value = "type", defaultValue = "cv") String fileType
    );

    /**
     * Met à jour les fichiers d'une candidature (PATCH)
     * PATCH /candidatures/{id}?cvPath=...&lettreMotivationPath=...
     */
    @PatchMapping("/candidatures/{id}")
    CandidatureDTO updateFiles(
        @PathVariable("id") String id,
        @RequestParam(value = "cvPath", required = false) String cvPath,
        @RequestParam(value = "lettreMotivationPath", required = false) String lettreMotivationPath
    );
}


