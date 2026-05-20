package com.hirehub.email.feign;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OffreServiceClientFallback implements OffreServiceClientAPI {
    @Override
    public OffreInfoDTO getOffreById(String id) {
        log.warn("[Feign Fallback] Impossible de récupérer l'offre {} depuis offre-service", id);
        return new OffreInfoDTO(id, "Offre indisponible", "", null);
    }
}
