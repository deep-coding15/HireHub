package com.hirehub.common.dtos.entretiens;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class EntretienPlanifiedDTO {
    @NotBlank
    @Email
    private String candidatEmail;

    @NotBlank
    private String candidatName;

    @NotBlank
    private String offreTitle;

    @NotBlank
    private String heureEntretien;

    @NotBlank
    private String candidatureId;

    private String commentaire;

    @NotBlank
    private String dateEntretien;

    @NotBlank
    private String lieuEntretien;

    private String interviewerName;
}
