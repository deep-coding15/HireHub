package com.hirehub.entretien.clients;

import com.hirehub.common.dtos.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CandidatureClientFallback implements CandidatureClient {

    @Override
    public ApiResponse<CandidatureSnapshot> getCandidatureById(String candidatureId) {
        log.warn("[Circuit Breaker] candidature-service indisponible : fallback pour candidature {}", candidatureId);
        return ApiResponse.error("Service candidatures temporairement indisponible");
    }

    @Override
    public ApiResponse<CandidatureSnapshot> updateStatus(String candidatureId, String newStatus) {
        log.warn("[Circuit Breaker] candidature-service indisponible : impossible de mettre à jour le statut de {}", candidatureId);
        return ApiResponse.error("Service candidatures temporairement indisponible");
    }
}
