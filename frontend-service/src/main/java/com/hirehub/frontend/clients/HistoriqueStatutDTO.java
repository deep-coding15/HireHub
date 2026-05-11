package com.hirehub.frontend.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO pour l'historique des changements de statut
 * Correspond à l'entité HistoriqueStatus du candidature-service
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutDTO {
    private String id;
    private String candidatureId;
    private String ancienStatus;
    private String nouveauStatus;
    private String commentaire;
    private LocalDateTime dateChangement;
    private String utilisateurId;
}

