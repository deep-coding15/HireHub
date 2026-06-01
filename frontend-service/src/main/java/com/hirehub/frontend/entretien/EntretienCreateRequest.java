package com.hirehub.frontend.entretien;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EntretienCreateRequest {
    private String candidatureId;
    private String candidatId;
    private String recruteurId;
    private LocalDateTime dateHeure;
    private String lieu;
    private String lienVisio;
    private String type;          // PRESENTIEL | VISIO | TELEPHONIQUE
    private String notesInternes;
}
