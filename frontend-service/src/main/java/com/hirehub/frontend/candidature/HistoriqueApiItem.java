package com.hirehub.frontend.candidature;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HistoriqueApiItem {
    private String timestamp;
    private String ancienStatut;
    private String nouveauStatut;
    private String auteur;
    private String commentaire;
}
