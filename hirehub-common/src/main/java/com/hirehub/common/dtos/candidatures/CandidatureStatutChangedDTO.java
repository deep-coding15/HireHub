package com.hirehub.common.dtos.candidatures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class CandidatureStatutChangedDTO {
    @NotNull
    Long candidatureId;

    @NotBlank
    @Email
    String candidatEmail;

    @NotBlank
    String candidatName;

    @NotBlank
    String offreTitle;

    @NotBlank
    String ancienStatut;

    @NotBlank
    String nouveauStatut;

    String commentaire;
}
