package com.hirehub.frontend.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO pour transférer les données de candidature
 * Correspond à l'entité Candidature du candidature-service
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureDTO {
    private String id;
    private String candidatId;
    private String offreId;
    private String cvPath;
    private String lettreMotivationPath;
    private String status; // SOUMISE, EN_COURS, ENTRETIEN, ACCEPTEE, REFUSEE
    private LocalDateTime dateSoumission;
    private LocalDateTime dateModification;
}

