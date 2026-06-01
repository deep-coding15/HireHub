package com.hirehub.email.listener;

import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.email.EmailBusinessServiceImpl;
import com.hirehub.email.feign.AuthServiceClientAPI;
import com.hirehub.email.feign.UserInfoDTO;
import com.hirehub.email.service.IdempotenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listener pour les événements de candidature.
 * Consomme les événements RabbitMQ et envoie les emails correspondants.
 * Utilise l'idempotence pour éviter les envois en double.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CandidatureListener {

    private final EmailBusinessServiceImpl emailService;
    private final IdempotenceService idempotenceService;
    private final AuthServiceClientAPI authServiceClientAPI;

    /**
     * Écoute les événements de création de candidature.
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE)
    public void handleCandidatureCreated(@Payload EmailEventDTO event) {
        try {
            if (event.getCorrelationId() != null) {
                MDC.put("correlationId", event.getCorrelationId());
            }
            String eventId = event.getEventId();

            // Vérifier l'idempotence
            if (idempotenceService.isAlreadyProcessed(eventId)) {
                log.warn("[CANDIDATURE.CREATED] Événement {} déjà traité, abandon", eventId);
                return;
            }

            log.info("[CANDIDATURE.CREATED] Traitement de l'événement {} pour: {}", eventId, event.getRecipientEmail());

            String offerTitle = event.getPayload() != null && event.getPayload().get("offerTitle") != null
                    ? event.getPayload().get("offerTitle").toString()
                    : "Offre";
            String candidateName = event.getRecipientName() != null && !event.getRecipientName().isBlank()
                    ? event.getRecipientName()
                    : "Candidat";

            emailService.sendCandidatureCreatedEmail(
                    event.getRecipientEmail(),
                    candidateName,
                    offerTitle
            );

            // Marquer comme traité avec succès
            idempotenceService.markAsProcessed(eventId, event.getEventType(), event.getRecipientEmail());
            log.info("[CANDIDATURE.CREATED] OK - Email envoye a: {}", event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[CANDIDATURE.CREATED] ERREUR lors du traitement", e);
            try {
                idempotenceService.markAsFailed(event.getEventId(), event.getEventType(), event.getRecipientEmail(), e.getMessage());
            } catch (Exception ex) {
                log.error("[IDEMPOTENCE ERROR] Impossible de marquer l'événement comme échoué: {}", ex.getMessage());
            }
            throw new RuntimeException("Erreur traitement création candidature", e);
        } finally {
            MDC.clear();
        }
    }

    private static final java.util.Set<String> STATUTS_AVEC_EMAIL =
            java.util.Set.of("ACCEPTEE", "REFUSEE", "ENTRETIEN");

    /**
     * Écoute les événements de changement de statut de candidature.
     * Envoie un email uniquement pour ACCEPTEE, REFUSEE et ENTRETIEN.
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
    public void handleCandidatureStatutChanged(@Payload EmailEventDTO event) {
        try {
            if (event.getCorrelationId() != null) {
                MDC.put("correlationId", event.getCorrelationId());
            }
            String eventId   = event.getEventId();
            String newStatus = event.getPayload() != null
                    ? (String) event.getPayload().get("newStatus") : null;

            // Ignorer silencieusement les statuts sans email (ex : EN_COURS)
            if (newStatus == null || !STATUTS_AVEC_EMAIL.contains(newStatus.toUpperCase())) {
                log.debug("[CANDIDATURE.STATUT.CHANGED] Statut {} sans notification email — ignoré", newStatus);
                return;
            }

            // Idempotence
            if (idempotenceService.isAlreadyProcessed(eventId)) {
                log.warn("[CANDIDATURE.STATUT.CHANGED] Événement {} déjà traité, abandon", eventId);
                return;
            }

            log.info("[CANDIDATURE.STATUT.CHANGED] Traitement '{}' eventId={} pour: {}",
                    newStatus, eventId, event.getRecipientEmail());

            String offerTitle = (String) event.getPayload().get("offerTitle");
            String oldStatus  = (String) event.getPayload().get("oldStatus");
            String comment    = (String) event.getPayload().get("comment");

            // L'email du candidat vient du payload — source de vérité
            String candidatEmail = event.getRecipientEmail();
            String candidatName  = event.getRecipientName();

            // Feign : enrichir le NOM uniquement (best-effort, jamais bloquant)
            if (event.getPayload().get("candidatId") != null) {
                try {
                    UserInfoDTO user = authServiceClientAPI.getUserById(
                            event.getPayload().get("candidatId").toString());
                    if (user != null && user.getFirstName() != null && !user.getFirstName().isBlank()) {
                        candidatName = user.getFirstName();
                    }
                } catch (Exception ex) {
                    log.warn("[CANDIDATURE.STATUT.CHANGED] Nom candidat indisponible: {}", ex.getMessage());
                }
            }

            emailService.sendCandidatureStatutChangedNotification(
                    candidatEmail, candidatName, offerTitle, oldStatus, newStatus, comment);

            idempotenceService.markAsProcessed(eventId, event.getEventType(), candidatEmail);
            log.info("[CANDIDATURE.STATUT.CHANGED] OK '{}' → {}", newStatus, candidatEmail);

        } catch (Exception e) {
            log.error("[CANDIDATURE.STATUT.CHANGED] ERREUR lors du traitement", e);
            try {
                idempotenceService.markAsFailed(
                        event.getEventId(), event.getEventType(),
                        event.getRecipientEmail(), e.getMessage());
            } catch (Exception ignored) {}
            throw new RuntimeException("Erreur traitement changement statut", e);
        } finally {
            MDC.clear();
        }
    }

}
