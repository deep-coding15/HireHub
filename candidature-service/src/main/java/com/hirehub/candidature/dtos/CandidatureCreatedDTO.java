package com.hirehub.candidature.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hirehub.common.enums.CandidatureStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO pour les candidatures dans les réponses API
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureCreatedDTO {
    @NotBlank
    private String id;
    @NotBlank
    private String candidatId;
    @NotBlank
    private String offreId;
    @NotBlank
    private String cvPath;
    @NotBlank
    private String lettreMotivationPath;
    @NotBlank
    private CandidatureStatus status;
}

