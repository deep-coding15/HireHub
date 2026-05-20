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
public class CandidatureRabbitDTO {
    private String id;

    @NotBlank(message = "candidatId is required")
    private String candidatId;

    @NotBlank(message = "offreId is required")
    private String offerId;
    private String offerTitle;
    private String CandidateEmail;
    private String status;
    private String cvPath;
    private String lettreMotivationPath;

    public CandidatureRabbitDTO(String candidatId, String offerId,
                                String offerTitle, String CandidateEmail, String status,
                          String cvPath, String lettreMotivationPath) {

        this.candidatId = candidatId;
        this.offerId = offerId;
        this.offerTitle = offerTitle;
        this.CandidateEmail = CandidateEmail;
        this.status = status;
        this.cvPath = cvPath;
        this.lettreMotivationPath = lettreMotivationPath;
    }
}
