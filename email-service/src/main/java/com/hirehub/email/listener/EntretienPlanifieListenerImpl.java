package com.hirehub.email.listener;

import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.email.EmailBusinessServiceImpl;
import com.hirehub.email.service.IdempotenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listener pour les événements d'entretien (planifié / annulé).
 * Un seul @RabbitListener sur la queue — dispatch par eventType.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntretienPlanifieListenerImpl {

    private final EmailBusinessServiceImpl emailService;
    private final IdempotenceService idempotenceService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void handleEntretienEvent(@Payload EmailEventDTO event) {
        try {
            if (event.getCorrelationId() != null) {
                MDC.put("correlationId", event.getCorrelationId());
            }

            String eventId   = event.getEventId();
            String eventType = event.getEventType();

            if (idempotenceService.isAlreadyProcessed(eventId)) {
                log.warn("[ENTRETIEN] Événement {} déjà traité, abandon", eventId);
                return;
            }

            log.info("[ENTRETIEN] Traitement '{}' eventId={} → {}", eventType, eventId, event.getRecipientEmail());

            switch (eventType != null ? eventType : "") {
                case "ENTRETIEN_PLANIFIE" -> handlePlanifie(event);
                case "ENTRETIEN_ANNULE"   -> handleAnnule(event);
                default -> log.warn("[ENTRETIEN] Type non reconnu: {}", eventType);
            }

            idempotenceService.markAsProcessed(eventId, eventType, event.getRecipientEmail());
            log.info("[ENTRETIEN] OK '{}' → {}", eventType, event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[ENTRETIEN] ERREUR lors du traitement", e);
            idempotenceService.markAsFailed(
                    event.getEventId(), event.getEventType(),
                    event.getRecipientEmail(), e.getMessage());
            throw new RuntimeException("Erreur traitement événement entretien", e);
        } finally {
            MDC.clear();
        }
    }

    private void handlePlanifie(EmailEventDTO event) {
        var p = event.getPayload();
        String offerTitle        = str(p.get("offerTitle"));
        String interviewDate     = str(p.get("interviewDate"));
        String interviewLocation = str(p.get("interviewLocation"));
        String consignes         = str(p.get("consignes"));

        // Enrichir la location avec le type si visio
        String lienVisio = str(p.get("lienVisio"));
        String location  = (lienVisio != null && !lienVisio.isBlank()) ? lienVisio
                         : (interviewLocation != null ? interviewLocation : "Téléphonique");

        emailService.sendEntretienPlanification(
                event.getRecipientEmail(),
                event.getRecipientName(),
                offerTitle,
                interviewDate,
                location,
                consignes   // dernier param = consignes (renommé depuis "interviewer" dans le template)
        );
    }

    private void handleAnnule(EmailEventDTO event) {
        var p = event.getPayload();
        emailService.sendEntretienAnnulation(
                event.getRecipientEmail(),
                event.getRecipientName(),
                str(p.get("offerTitle")),
                str(p.get("comment"))
        );
    }

    private static String str(Object o) {
        return o instanceof String s ? s : (o != null ? o.toString() : null);
    }
}
