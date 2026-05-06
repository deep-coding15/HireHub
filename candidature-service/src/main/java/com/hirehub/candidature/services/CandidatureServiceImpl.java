package com.hirehub.candidature.services;

import com.hirehub.candidature.clients.OffreServiceClient;
import com.hirehub.candidature.config.CandidatureStateMachine;
import com.hirehub.candidature.config.InvalidTransitionException;
import com.hirehub.candidature.config.UserContext;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.candidature.exceptions.CandidatureChangedStatusException;
import com.hirehub.candidature.exceptions.CandidatureCreatedConflitException;
import com.hirehub.candidature.exceptions.CandidatureNotFoundException;
import com.hirehub.candidature.exceptions.OffreNotFoundException;
import com.hirehub.candidature.repository.CandidatureRepository;
import com.hirehub.candidature.repository.HistoriqueStatusRepository;
import com.hirehub.common.constants.EventType;
import com.hirehub.common.enums.CandidatureStatus;
import com.hirehub.common.notification.NotificationPublisher;
import com.hirehub.common.notification.RabbitMQConstants;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation réelle du service CandidatureService
 * Utilise PostgreSQL via JPA et publie des événements RabbitMQ
 */
@Service
@Profile("!mock") // S'active partout SAUF si le profil 'mock' est actif
@Slf4j
public class CandidatureServiceImpl implements CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final HistoriqueStatusRepository historiqueStatusRepository;
    private final NotificationPublisher notificationPublisher;
    private final OffreServiceClient offreServiceClient;

    public CandidatureServiceImpl(
            CandidatureRepository candidatureRepository,
            HistoriqueStatusRepository historiqueStatusRepository,
            NotificationPublisher notificationPublisher,
            OffreServiceClient offreServiceClient
    ) {
        this.candidatureRepository = candidatureRepository;
        this.historiqueStatusRepository = historiqueStatusRepository;
        this.notificationPublisher = notificationPublisher;
        this.offreServiceClient = offreServiceClient;
    }

    @Override
    public void createCandidatureByCandidat(Candidature candidature) {
        log.info("Création d'une candidature pour l'offre: {}", candidature.getOffreId());

        // 1. Vérifier que l'offre existe et est publiée
        try {
            boolean offreExists = offreServiceClient.offreExists(candidature.getOffreId());
            if (!offreExists) {
                log.warn("Offre {} non trouvée ou non publiée", candidature.getOffreId());
                throw new OffreNotFoundException("L'offre n'existe pas ou n'est pas publiée.");
            }
        } catch (FeignException e) {
            log.error("Erreur lors de la vérification de l'offre: {}", e.getMessage(), e);
            throw new OffreNotFoundException(candidature.getOffreId(), e);
        }

        // 2. Vérifier qu'un candidat n'a postulé qu'une seule fois par offre
        Optional<Candidature> existing = candidatureRepository.findByCandidatIdAndOffreId(
                candidature.getCandidatId(),
                candidature.getOffreId()
        );

        if (existing.isPresent()) {
            log.warn("Candidat {} a déjà postulé à l'offre {}",
                    candidature.getCandidatId(), candidature.getOffreId());
            throw new CandidatureCreatedConflitException(
                    "Vous avez déjà postulé à cette offre.");
        }

        // 3. Définir les valeurs par défaut
        candidature.setStatus(CandidatureStatus.EN_COURS);
        candidature.setDateSoumission(LocalDateTime.now());

        // 4. Sauvegarder en BDD
        Candidature saved = candidatureRepository.save(candidature);
        log.info("Candidature créée avec l'ID: {}", saved.getId());

        // 5. Publier l'événement RabbitMQ
        publishCandidatureCreatedEvent(saved);
    }

    @Override
    public List<Candidature> getMyCandidaturesByCandidat() {
        log.info("Récupération des candidatures du candidat");
        // TODO: Récupérer le candidatId depuis le SecurityContext
        String candidatId = "current-user"; // À remplacer par le vrai candidat authentifié
        return candidatureRepository.findByCandidatId(candidatId);
    }

    @Override
    public List<Candidature> getCandidaturesByOfferIdByRecruiter(String offerId) {
        log.info("Récupération des candidatures pour l'offre: {}", offerId);
        // TODO: Vérifier que le recruteur authentifié est propriétaire de l'offre
        return candidatureRepository.findByOffreId(offerId);
    }

    @Override
    public Candidature getCandidatureById(String id) {
        log.info("Récupération de la candidature: {}", id);
        return candidatureRepository.findById(id).orElse(null);
    }

    @Override
    public void updateCandidatureStatusByRecruiter(String id, String status) {
        log.info("Mise à jour du statut de la candidature: {} -> {}", id, status);

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow( () -> new CandidatureNotFoundException("Candidature non trouvée"));

        try {
            CandidatureStatus newStatus = CandidatureStatus.valueOf(status);
            CandidatureStatus oldStatus = candidature.getStatus();

            // 1. Valider la transition de statut (machine d'états)
            if (!CandidatureStateMachine.isTransitionValid(oldStatus.name(), newStatus.name())) {
                log.warn("Transition invalide: {} -> {}", oldStatus, newStatus);
                throw new InvalidTransitionException(oldStatus.name(), newStatus.name());
            }

            // 2. Mettre à jour le statut
            candidature.setStatus(newStatus);
            candidature.setDateModification(LocalDateTime.now());
            candidatureRepository.save(candidature);

            // 3. Enregistrer dans l'historique
            HistoriqueStatus historique = new HistoriqueStatus();
            historique.setCandidatureId(id);
            historique.setAncienStatus(oldStatus);
            historique.setNouveauStatus(newStatus);
            historique.setDateChangement(LocalDateTime.now());

            // Récupérer l'ID du recruteur depuis UserContext
            UserContext.UserInfo user = UserContext.getUser();
            if (user != null) {
                historique.setUtilisateurId(user.userId.toString());
            } else {
                historique.setUtilisateurId("system");
            }
            historiqueStatusRepository.save(historique);

            log.info("Statut de la candidature {} mis à jour de {} à {}", id, oldStatus, newStatus);

            // 4. Publier l'événement RabbitMQ
            publishStatutChangedEvent(candidature, oldStatus, newStatus);

        } catch (IllegalArgumentException e) {
            log.error("Statut invalide: {}", status);
            throw new CandidatureChangedStatusException("Statut invalide: " + status);
        } catch (InvalidTransitionException e) {
            log.warn("Transition invalide: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateCandidatureDetailsByCandidat(String id, String CV_Path, String lettreMotivationPath) {
        log.info("Mise à jour des détails de la candidature: {}", id);

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée"));

        // TODO: Vérifier que le candidat authentifié est le propriétaire

        if (CV_Path != null) {
            candidature.setCV_Path(CV_Path);
        }
        if (lettreMotivationPath != null) {
            candidature.setLettreMotivationPath(lettreMotivationPath);
        }

        candidature.setDateModification(LocalDateTime.now());
        candidatureRepository.save(candidature);

        log.info("Détails de la candidature {} mis à jour", id);
    }

    @Override
    public void uploadCVAndCoverLetter(String id, String CV_Path, String lettreMotivationPath) {
        log.info("Upload des fichiers pour la candidature: {}", id);
        // TODO: Implémenter l'upload réel des fichiers
        updateCandidatureDetailsByCandidat(id, CV_Path, lettreMotivationPath);
    }

    @Override
    public void deleteCandidatureByCandidat(String id) {
        log.info("Suppression de la candidature: {}", id);

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // TODO: Vérifier que le candidat authentifié est le propriétaire

        // Supprimer aussi l'historique associé
        List<HistoriqueStatus> historique = historiqueStatusRepository.findByCandidatureIdOrderByDateChangementDesc(id);
        historiqueStatusRepository.deleteAll(historique);

        // Supprimer la candidature
        candidatureRepository.deleteById(id);

        log.info("Candidature {} supprimée", id);
    }

    /**
     * Publie un événement "candidature.created" dans RabbitMQ (contrat EmailEventDTO)
     */
    private void publishCandidatureCreatedEvent(Candidature candidature) {
        try {
            // TODO: en attendant l'intégration auth, on n'a pas l'email ici.
            // On met des placeholders pour tester le flux.
            String candidateEmail = "test@hirehub.local";
            String candidateName = "Candidat";

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("candidatureId", candidature.getId());
            payload.put("offerId", candidature.getOffreId());
            payload.put("offerTitle", "Offre");

            notificationPublisher.publishEmailEvent(
                    EventType.CANDIDATURE_CREATED,
                    candidateEmail,
                    candidateName,
                    RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
                    payload
            );

            log.info("Événement 'candidature.created' (EmailEventDTO) publié pour la candidature: {}", candidature.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement candidature.created: {}", e.getMessage(), e);
        }
    }

    /**
     * Publie un événement "candidature.statut.changed" dans RabbitMQ (contrat EmailEventDTO)
     */
    private void publishStatutChangedEvent(Candidature candidature, CandidatureStatus oldStatus, CandidatureStatus newStatus) {
        try {
            String candidateEmail = "test@hirehub.local";
            String candidateName = "Candidat";

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("candidatureId", candidature.getId());
            payload.put("offerId", candidature.getOffreId());
            payload.put("offerTitle", "Offre");
            payload.put("oldStatus", oldStatus.name());
            payload.put("newStatus", newStatus.name());
            payload.put("comment", "");

            notificationPublisher.publishEmailEvent(
                    EventType.CANDIDATURE_STATUT_CHANGED,
                    candidateEmail,
                    candidateName,
                    RabbitMQConstants.ROUTING_CANDIDATURE_STATUT_CHANGED,
                    payload
            );

            log.info("Événement 'candidature.statut.changed' (EmailEventDTO) publié pour la candidature: {}", candidature.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement statut.changed: {}", e.getMessage(), e);
        }
    }
}
