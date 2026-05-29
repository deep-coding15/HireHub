package com.hirehub.candidature.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO envoyé par le client lors de la création d'une candidature.
 * Seul {@code offreId} est requis ; les autres champs sont optionnels ou
 * déterminés côté serveur (id, candidatId, status).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureCreatedDTO {

    private String id;
    private String candidatId;

    @NotBlank(message = "L'identifiant de l'offre est requis")
    private String offreId;

    private String cvPath;
    private String lettreMotivationPath;
    private com.hirehub.common.enums.CandidatureStatus status;
}
