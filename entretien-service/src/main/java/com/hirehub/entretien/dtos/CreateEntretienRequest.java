package com.hirehub.entretien.dtos;

import com.hirehub.entretien.entities.EntretienType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class CreateEntretienRequest {
    private String candidatureId;
    private String candidatId;      // fourni par le frontend (évite l'appel Feign interne)
    private String candidatEmail;   // pour la notification email directe
    private String candidatNom;     // pour personnaliser l'email
    private String offreTitre;      // pour le corps de l'email
    private String recruteurId;
    private LocalDateTime dateHeure;
    private String lieu;
    private String lienVisio;
    private EntretienType type;
    private String notesInternes;
}