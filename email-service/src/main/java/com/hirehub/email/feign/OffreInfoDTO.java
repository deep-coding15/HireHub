package com.hirehub.email.feign;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OffreInfoDTO {
    private String id;
    private String titre;
    private String description;
    private String recruteurId;
}
