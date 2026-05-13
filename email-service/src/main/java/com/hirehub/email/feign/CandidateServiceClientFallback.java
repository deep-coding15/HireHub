package com.hirehub.email.feign;

import com.hirehub.email.dto.CandidateInfoDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CandidateServiceClientFallback implements CandidateServiceClientAPI {

    @Override
    public CandidateInfoDTO getCandidateById(String id) {
        log.warn("[Feign Fallback] Impossible de récupérer le candidat {} depuis candidature-service", id);
        return new CandidateInfoDTO(null, null, null, null);
    }
}

