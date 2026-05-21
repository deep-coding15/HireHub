package com.hirehub.entretien.services;

import com.hirehub.entretien.dtos.CreateEntretienRequest;
import com.hirehub.entretien.entities.Entretien;

import java.util.List;

public interface EntretienService {
    Entretien create(CreateEntretienRequest request);
    List<Entretien> listByCandidature(String candidatureId);
    List<Entretien> listByRecruteur(String recruteurId);
    List<Entretien> listByCandidat(String candidatId);
    Entretien cancel(String entretienId, String recruteurId);
}