package com.hirehub.candidature.seed;

import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.candidature.repository.CandidatureRepository;
import com.hirehub.candidature.repository.HistoriqueStatusRepository;
import com.hirehub.common.enums.CandidatureStatus;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeder qui insère des candidatures et des entrées d'historique si la table candidatures est vide.
 * Activé sur tous les profils sauf "test".
 */
@Component
@Order(100)
@Profile("!test")
public class CandidatureDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CandidatureDataSeeder.class);

    private final CandidatureRepository candidatureRepository;
    private final HistoriqueStatusRepository historiqueStatusRepository;

    public CandidatureDataSeeder(CandidatureRepository candidatureRepository,
                                 HistoriqueStatusRepository historiqueStatusRepository) {
        this.candidatureRepository = candidatureRepository;
        this.historiqueStatusRepository = historiqueStatusRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            long count = candidatureRepository.count();
            if (count > 0) {
                log.info("CandidatureDataSeeder: database already has {} candidature(s), skipping seeder.", count);
                return;
            }

            log.info("CandidatureDataSeeder: no candidatures found, seeding sample data...");

            List<Candidature> toSave = new ArrayList<>();

            Candidature c1 = new Candidature();
            c1.setCandidatId("candidat-1");
            c1.setOffreId("offre-1001");
            c1.setCvPath(null);
            c1.setLettreMotivationPath(null);
            c1.setStatus(CandidatureStatus.SOUMISE);

            Candidature c2 = new Candidature();
            c2.setCandidatId("candidat-2");
            c2.setOffreId("offre-1002");
            c2.setCvPath(null);
            c2.setLettreMotivationPath(null);
            c2.setStatus(CandidatureStatus.EN_COURS);

            toSave.add(c1);
            toSave.add(c2);

            List<Candidature> saved = candidatureRepository.saveAll(toSave);

            // Seed historique entries
            List<HistoriqueStatus> histos = new ArrayList<>();
            for (Candidature savedC : saved) {
                HistoriqueStatus h = new HistoriqueStatus();
                h.setCandidatureId(savedC.getId());
                h.setAncienStatus(null);
                h.setNouveauStatus(savedC.getStatus());
                h.setCommentaire("Initial import / seed");
                h.setUtilisateurId("system-seeder");
                h.setDateChangement(LocalDateTime.now());
                histos.add(h);
            }
            historiqueStatusRepository.saveAll(histos);

            log.info("CandidatureDataSeeder: inserted {} candidatures and {} historique entries.", saved.size(), histos.size());

        } catch (Exception e) {
            log.error("CandidatureDataSeeder: error while seeding data", e);
        }
    }
}