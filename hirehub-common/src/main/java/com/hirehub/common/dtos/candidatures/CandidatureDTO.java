package com.hirehub.common.dtos.candidatures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidatureDTO {
    private String id;

    @NotBlank(message = "candidatId is required")
    private String candidatId;

    @NotBlank(message = "offreId is required")
    private String offreId;

    private String status;
    private String cvPath;
    private String lettreMotivationPath;
}
