package com.hirehub.candidature.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidatureResponseDTO {
    private String id;
    private String offreId;
    private String candidatId;
    private String candidatEmail;
    private String status;
    private String cvPath;
    private String lettreMotivationPath;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateModification;
}
