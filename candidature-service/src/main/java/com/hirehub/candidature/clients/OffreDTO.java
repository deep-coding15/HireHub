package com.hirehub.candidature.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO pour les offres d'emploi (depuis offre-service).
 * Miroir de OffreResponse : le champ {@code statut} vaut "PUBLIEE", "BROUILLON" ou "FERMEE".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OffreDTO {
    private String id;
    private String titre;
    private String description;
    private String recruteurId;
    private String statut;

    public boolean isPublished() {
        return "PUBLIEE".equals(statut);
    }
}

