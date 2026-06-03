package com.hirehub.frontend.clients;

import com.hirehub.common.dtos.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class CandidatureServiceClientFallback implements CandidatureServiceClient {

    private static final String MSG = "Service candidatures temporairement indisponible";

    @Override
    public ApiResponse<List<CandidatureDTO>> getMyCandidatures() {
        log.warn("[Circuit Breaker] candidature-service indisponible : retourne liste vide");
        return ApiResponse.ok(Collections.emptyList());
    }

    @Override
    public ApiResponse<CandidatureDTO> getCandidature(String id) {
        log.warn("[Circuit Breaker] candidature-service indisponible : fallback pour candidature {}", id);
        return ApiResponse.error(MSG);
    }

    @Override
    public ApiResponse<CandidatureDTO> createCandidature(CandidatureDTO dto) {
        log.warn("[Circuit Breaker] candidature-service indisponible : création impossible");
        return ApiResponse.error(MSG);
    }

    @Override
    public ApiResponse<CandidatureDTO> updateStatus(String id, String status) {
        log.warn("[Circuit Breaker] candidature-service indisponible : mise à jour statut impossible");
        return ApiResponse.error(MSG);
    }

    @Override
    public ApiResponse<Void> deleteCandidature(String id) {
        log.warn("[Circuit Breaker] candidature-service indisponible : suppression impossible");
        return ApiResponse.error(MSG);
    }

    @Override
    public ApiResponse<List<HistoriqueStatutDTO>> getHistorique(String id) {
        log.warn("[Circuit Breaker] candidature-service indisponible : historique indisponible");
        return ApiResponse.ok(Collections.emptyList());
    }

    @Override
    public ApiResponse<List<CandidatureDTO>> getCandidaturesByOffre(String offreId) {
        log.warn("[Circuit Breaker] candidature-service indisponible : liste par offre vide");
        return ApiResponse.ok(Collections.emptyList());
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(String id, String fileType) {
        log.warn("[Circuit Breaker] candidature-service indisponible : téléchargement impossible");
        return ResponseEntity.unprocessableEntity().build();
    }

    @Override
    public ApiResponse<CandidatureDTO> updateFiles(String id, String cvPath, String lettreMotivationPath) {
        log.warn("[Circuit Breaker] candidature-service indisponible : mise à jour fichiers impossible");
        return ApiResponse.error(MSG);
    }
}
