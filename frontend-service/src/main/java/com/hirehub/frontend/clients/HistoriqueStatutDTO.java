package com.hirehub.frontend.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mirrors candidature-service's HistoriqueStatusDTO output:
 * {timestamp, ancienStatut, nouveauStatut, auteur, commentaire}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutDTO {
    private String timestamp;
    private String ancienStatut;
    private String nouveauStatut;
    private String auteur;
    private String commentaire;
}
