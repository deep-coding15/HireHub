package com.hirehub.candidature.services;

import com.hirehub.candidature.clients.IOffreServiceClient;
import com.hirehub.candidature.config.CandidatureStateMachine;
import com.hirehub.candidature.config.InvalidTransitionException;
import com.hirehub.candidature.config.UserContext;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.candidature.exceptions.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implémentation réelle du service ICandidatureService
 * Utilise PostgreSQL via JPA et publie des événements RabbitMQ
 */
@Service
@Profile("!mock") // S'active partout SAUF si le profil 'mock' est actif
@Slf4j
public class CandidatureServiceImpl implements ICandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final HistoriqueStatusRepository historiqueStatusRepository;
    private final NotificationPublisher notificationPublisher;
    private final IOffreServiceClient iOffreServiceClient;

    public CandidatureServiceImpl(
            CandidatureRepository candidatureRepository,
            HistoriqueStatusRepository historiqueStatusRepository,
            NotificationPublisher notificationPublisher,
            IOffreServiceClient iOffreServiceClient
    ) {
        this.candidatureRepository = candidatureRepository;
        this.historiqueStatusRepository = historiqueStatusRepository;
        this.notificationPublisher = notificationPublisher;
        this.iOffreServiceClient = iOffreServiceClient;
    }

    @Override
    public Candidature createCandidatureByCandidat(Candidature candidature) {
        log.info("Création d'une candidature pour l'offre: {}", candidature.getOffreId());

        // 1. Vérifier que l'offre existe et est publiée
        try {
            boolean offreExists = iOffreServiceClient.offreExists(candidature.getOffreId());
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
        candidature.setStatus(CandidatureStatus.SOUMISE);
        candidature.setDateSoumission(LocalDateTime.now());

        // 4. Sauvegarder en BDD
        Candidature saved = candidatureRepository.save(candidature);
        log.info("Candidature créée avec l'ID: {}", saved.getId());

        // 5. Publier l'événement RabbitMQ
        publishCandidatureCreatedEvent(saved);
        return saved;
    }

    @Override
    public List<Candidature> getMyCandidaturesByCandidat() {

        log.info("Récupération des candidatures du candidat");
        // Récupérer le candidatId depuis le SecurityContext
        UserContext.UserInfo user = UserContext.getUser();
        if (user == null) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }
        String candidatId = user.userId.toString();

        return candidatureRepository.findByCandidatId(candidatId);
    }

    @Override
    public List<Candidature> getCandidaturesByOfferIdByRecruiter(String offerId) {

        log.info("Récupération des candidatures pour l'offre: {}", offerId);
        // Vérifier que le recruteur authentifié est propriétaire de l'offre
        UserContext.UserInfo user = UserContext.getUser();
        if (user == null) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }

        try {
            boolean isOwner = iOffreServiceClient.isRecruteurOwner(offerId, user.userId.toString());
            if (!isOwner) {
                log.warn("Recruteur {} n'est pas propriétaire de l'offre {}", user.userId, offerId);
                throw new UnauthorizedException("Vous n'êtes pas propriétaire de cette offre");
            }
        } catch (FeignException.NotFound e) {
            log.error("Offre {} non trouvée", offerId, e);
            throw new OffreNotFoundException("Offre non trouvée");
        } catch (FeignException e) {
            log.error("Erreur lors de la vérification de propriété de l'offre: {}", e.getMessage(), e);
            throw new UnauthorizedException("Erreur de vérification d'accès à l'offre");
        }
        return candidatureRepository.findByOffreId(offerId);
    }

    @Override
    public Candidature getCandidatureById(String candidatureId) {

        UserContext.UserInfo userInfo = UserContext.getUser();

        if (userInfo == null) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }

        String userId = userInfo.userId.toString();

        log.info("Récupération de la candidature: {}", candidatureId);

        Candidature existing = candidatureRepository.findById(candidatureId)
                .orElseThrow(() ->
                        new CandidatureNotFoundException("Candidature non trouvée"));

        // Vérifier que l'utilisateur est soit le candidat, soit le recruteur propriétaire de l'offre

        // Candidat propriétaire
        if (existing.getCandidatId() != null &&
                existing.getCandidatId().equals(userId)) {
            return existing;
        }

        // Recruteur propriétaire de l'offre
        try {
            boolean isOwner = iOffreServiceClient.isRecruteurOwner(
                    existing.getOffreId(), userId);
            if (isOwner) {
                return existing;
            }
        } catch (FeignException.NotFound e) {
            log.warn("Offre {} introuvable pour la candidature {}",
                    existing.getOffreId(), candidatureId);
            throw new OffreNotFoundException("Offre non trouvée");
        } catch (FeignException e) {
            log.error("Erreur de vérification de propriété d'offre: {}", e.getMessage(), e);
            throw new RuntimeException(
                    "Erreur de communication avec offre-service"
            );
        }

        // 3. Aucun droit d'accès
        throw new UnauthorizedException("Accès refusé à cette candidature");
    }

    @Override
    public void updateCandidatureStatusByRecruiter(String candidatureId, String status) {

        Candidature candidature = null;
        try {
            log.info("Mise à jour du statut de la candidature: {} -> {}", candidatureId, status);

            UserContext.UserInfo userInfo = UserContext.getUser();
            if (userInfo == null) {
                throw new UnauthorizedException("Utilisateur non authentifié");
            }

            candidature = candidatureRepository.findById(candidatureId)
                    .orElseThrow( () -> new CandidatureNotFoundException("Candidature non trouvée"));

            boolean isOwner = iOffreServiceClient.isRecruteurOwner(
                    candidature.getOffreId(), userInfo.userId.toString());

            if (!isOwner) {
                throw new UnauthorizedException("Vous n'etes pas propriétaire de l'offre qui a cette candidature.");
            }

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
            historique.setCandidatureId(candidatureId);
            historique.setAncienStatus(oldStatus);
            historique.setNouveauStatus(newStatus);
            historique.setUtilisateurId(userInfo.userId.toString());
            historique.setDateChangement(LocalDateTime.now());

            // Récupérer l'ID du recruteur depuis UserContext
            UserContext.UserInfo user = UserContext.getUser();
            if (user != null) {
                historique.setUtilisateurId(user.userId.toString());
            } else {
                historique.setUtilisateurId("system");
            }
            historiqueStatusRepository.save(historique);

            log.info("Statut de la candidature {} mis à jour de {} à {}", candidatureId, oldStatus, newStatus);

            // 4. Publier l'événement RabbitMQ
            publishStatutChangedEvent(candidature, oldStatus, newStatus);

        }
        catch (FeignException.NotFound e) {
            log.warn("Offre {} introuvable pour la candidature {}",
                    candidature.getOffreId(), candidatureId);
            throw new OffreNotFoundException("Offre non trouvée");
        }
        catch (IllegalArgumentException e) {
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

        // Vérifier que le candidat authentifié est le propriétaire
        UserContext.UserInfo user = UserContext.getUser();
        if (user == null) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }

        if (!candidature.getCandidatId().equals(user.userId.toString())) {
            log.warn("Candidat {} tente d'accéder à une candidature qui ne lui appartient pas ({})",
                user.userId, candidature.getId());
            throw new UnauthorizedException("Cette candidature ne vous appartient pas");
        }

        if (CV_Path != null) {
            candidature.setCvPath(CV_Path);
        }
        if (lettreMotivationPath != null) {
            candidature.setLettreMotivationPath(lettreMotivationPath);
        }

        candidature.setDateModification(LocalDateTime.now());
        candidatureRepository.save(candidature);

        log.info("Détails de la candidature {} mis à jour", id);
    }

    @Override
    public void uploadCVAndCoverLetter(String candidatureId, String CV_Path, String lettreMotivationPath) {
        log.info("Upload des fichiers pour la candidature: {}", candidatureId);

        // Vérifier que les chemins sont fournis
        if (CV_Path == null && lettreMotivationPath == null) {
            throw new IllegalArgumentException("Au moins un fichier (CV ou lettre de motivation) doit être fourni");
        }

        // Générer les chemins relatifs dans le volume Docker
        String cvRelativePath = null;
        String lettreRelativePath = null;

        if (CV_Path != null && !CV_Path.isEmpty()) {
            // Générer un nom de fichier unique pour le CV
            String fileName = "cv_" + candidatureId + "_" + System.currentTimeMillis() + ".pdf";
            cvRelativePath = "cvs/" + fileName;
            log.info("CV sera stocké dans: {}", cvRelativePath);
        }

        if (lettreMotivationPath != null && !lettreMotivationPath.isEmpty()) {
            // Générer un nom de fichier unique pour la lettre
            String fileName = "lettre_" + candidatureId + "_" + System.currentTimeMillis() + ".pdf";
            lettreRelativePath = "lettres/" + fileName;
            log.info("Lettre de motivation sera stockée dans: {}", lettreRelativePath);
        }

        // Mettre à jour la candidature avec les nouveaux chemins
        this.updateCandidatureDetailsByCandidat(candidatureId, cvRelativePath, lettreRelativePath);

        log.info("Upload des fichiers terminé pour la candidature {}", candidatureId);
    }

    @Override
    public void deleteCandidatureByCandidat(String candidatureId) {
        log.info("Suppression de la candidature: {}", candidatureId);

        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // TODO: Vérifier que le candidat authentifié est le propriétaire
        // Vérifier que le candidat authentifié est le propriétaire
        UserContext.UserInfo user = UserContext.getUser();
        if (user == null) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }

        if (!candidature.getCandidatId().equals(user.userId.toString())) {
            log.warn("Candidat {} tente de supprimer une candidature qui ne lui appartient pas ({})",
                    user.userId, candidature.getId());
            throw new UnauthorizedException("Cette candidature ne vous appartient pas");
        }

        // Supprimer aussi l'historique associé
        /*List<HistoriqueStatus> historique = historiqueStatusRepository
                .findByCandidatureIdOrderByDateChangementDesc(candidatureId);

        historiqueStatusRepository.deleteAll(historique);*/

        // Supprimer la candidature
        candidatureRepository.deleteById(candidatureId);

        log.info("Candidature {} supprimée", candidatureId);
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

            Map<String, Object> payload = new HashMap<>();
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

            Map<String, Object> payload = new HashMap<>();
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
