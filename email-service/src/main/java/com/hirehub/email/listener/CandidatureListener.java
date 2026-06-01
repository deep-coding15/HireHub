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

    /**
     * Écoute les événements de changement de statut de candidature.
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
    public void handleCandidatureStatutChanged(@Payload EmailEventDTO event) {
        try {
            if (event.getCorrelationId() != null) {
                MDC.put("correlationId", event.getCorrelationId());
            }
            String eventId = event.getEventId();
            log.info("[CANDIDATURE.STATUT.CHANGED] Traitement de l'événement {} pour: {}", eventId, event.getRecipientEmail());

            // Extraire les données du payload
            String offerTitle = (String) event.getPayload().get("offerTitle");
            String oldStatus = (String) event.getPayload().get("oldStatus");
            String newStatus = (String) event.getPayload().get("newStatus");
            String comment = (String) event.getPayload().get("comment");

            // L'email du candidat vient du payload — c'est la source de vérité
            String candidatEmail = event.getRecipientEmail();
            String candidatName = event.getRecipientName();

            // Feign : enrichir uniquement le NOM d'affichage (best-effort, jamais bloquant)
            if (event.getPayload().get("candidatId") != null) {
                try {
                    UserInfoDTO user = authServiceClientAPI.getUserById(
                            event.getPayload().get("candidatId").toString());
                    if (user != null && user.getFirstName() != null && !user.getFirstName().isBlank()) {
                        candidatName = user.getFirstName();
                    }
                } catch (Exception ex) {
                    log.warn("[CANDIDATURE.STATUT.CHANGED] Impossible de récupérer le nom du candidat: {}", ex.getMessage());
                }
            }

            emailService.sendCandidatureStatutChangedNotification(
                    candidatEmail,
                    candidatName,
                    offerTitle,
                    oldStatus,
                    newStatus,
                    comment
            );

            log.info("[CANDIDATURE.STATUT.CHANGED] OK - Email envoye a: {}", event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[CANDIDATURE.STATUT.CHANGED] ERREUR lors du traitement", e);
            throw new RuntimeException("Erreur traitement changement statut", e);
        } finally {
            MDC.clear();
        }
    }

}
