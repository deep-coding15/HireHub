package com.hirehub.email.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidatureEmailDTO {
    private String id;
    private String candidatEmail;
    private String candidatName;
    private String offreId;
    private String offreTitle;
}

