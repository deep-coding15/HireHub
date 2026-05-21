package com.hirehub.entretien.dtos;

import com.hirehub.entretien.entities.EntretienType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class CreateEntretienRequest {
    private String candidatureId;
    private String recruteurId;
    private LocalDateTime dateHeure;
    private String lieu;
    private String lienVisio;
    private EntretienType type;
    private String notesInternes;
}