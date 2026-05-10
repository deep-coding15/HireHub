package com.hirehub.candidature.config;

import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.repository.CandidatureRepository;
import com.hirehub.common.enums.CandidatureStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CandidatureSeeder implements CommandLineRunner {

    private final CandidatureRepository candidatureRepository;

    @Override
    public void run(String... args) throws Exception {
        // On vérifie si la base est vide pour éviter les doublons à chaque redémarrage
        if (candidatureRepository.count() == 0) {
            String idCandidatUnique = "00000000-0000-0000-0000-00000000000"; // L'ID de ton candidat de test

            List<Candidature> candidatures = List.of(
                    createCandidature(idCandidatUnique, "offre-java-001", "cv_dev_java.pdf", CandidatureStatus.SOUMISE),
                    createCandidature(idCandidatUnique, "offre-react-002", "cv_frontend.pdf", CandidatureStatus.ACCEPTEE),
                    createCandidature(idCandidatUnique, "offre-node-003", "cv_fullstack.pdf", CandidatureStatus.REFUSEE),
                    createCandidature(idCandidatUnique, "offre-devops-004", "cv_devops.pdf", CandidatureStatus.EN_COURS)
            );

            candidatureRepository.saveAll(candidatures);
            System.out.println(">> Database Seeded: 4 candidatures ajoutées pour le candidat " + idCandidatUnique);
        }
    }

    private Candidature createCandidature(String candidatId, String offreId, String cvName, CandidatureStatus status) {
        Candidature c = new Candidature();
        // L'ID est généré automatiquement via UUID dans ton entité
        c.setCandidatId(candidatId);
        c.setOffreId(offreId);
        c.setCV_Path("/uploads/cv/" + cvName);
        c.setLettreMotivationPath("/uploads/lm/lettre_" + offreId + ".pdf");
        c.setStatus(status);
        // dateSoumission et dateModification sont gérées par @PrePersist dans ton entité
        return c;
    }
}

