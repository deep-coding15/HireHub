package com.hirehub.candidature.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoriqueStatusDTO {
    private String timestamp;
    private String ancienStatut;
    private String nouveauStatut;
    private String auteur;
    private String commentaire;
}
