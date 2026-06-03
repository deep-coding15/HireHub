package com.hirehub.candidature.clients;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IOffreServiceClientFallback implements IOffreServiceClient {

    @Override
    public OffreDTO getOffre(String id) {
        log.warn("[Circuit Breaker] offre-service indisponible : fallback pour offre {}", id);
        return new OffreDTO(id, "Offre temporairement indisponible", "", null, "INDISPONIBLE");
    }

    @Override
    public boolean offreExists(String id) {
        log.warn("[Circuit Breaker] offre-service indisponible : offreExists({}) retourne false", id);
        return false;
    }

    @Override
    public boolean isRecruteurOwner(String offreId, String recruteurId) {
        log.warn("[Circuit Breaker] offre-service indisponible : isRecruteurOwner retourne false");
        return false;
    }
}
