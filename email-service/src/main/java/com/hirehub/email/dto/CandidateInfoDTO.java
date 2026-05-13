package com.hirehub.email.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateInfoDTO {
    public String email;
    public String firstName;
    public String lastName;
    public String fullName;
}
