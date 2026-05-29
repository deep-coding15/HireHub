package com.hirehub.frontend.candidature;

import com.hirehub.common.enums.CandidatureStatus;

public record CandidatureCreateRequest(
        String id,
        String candidatId,
        String offreId,
        String cvPath,
        String lettreMotivationPath,
        CandidatureStatus status
) {}
