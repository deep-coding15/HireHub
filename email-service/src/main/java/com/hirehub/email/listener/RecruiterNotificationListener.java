package com.hirehub.email.listener;

import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.email.EmailBusinessServiceImpl;
import com.hirehub.email.service.IdempotenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecruiterNotificationListener {

    private final EmailBusinessServiceImpl emailService;
    private final IdempotenceService idempotenceService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER)
    public void handleRecruiterDecision(@Payload EmailEventDTO event) {
        try {
            if (event.getEventId() != null && idempotenceService.isAlreadyProcessed(event.getEventId())) {
                log.warn("[RECRUITER] Événement {} déjà traité", event.getEventId());
                return;
            }

            String eventType = event.getEventType();
            String reason = event.getPayload() != null
                    ? (String) event.getPayload().get("decisionMessage")
                    : null;

            log.info("[RECRUITER.{}] Traitement pour {}", eventType, event.getRecipientEmail());

            switch (eventType) {
                case "RECRUITER.APPROVED" -> emailService.sendRecruiterApproved(
                        event.getRecipientEmail(), event.getRecipientName());
                case "RECRUITER.REJECTED" -> emailService.sendRecruiterRejected(
                        event.getRecipientEmail(), event.getRecipientName(), reason);
                case "RECRUITER.REVIEW_REQUIRED" -> emailService.sendRecruiterReviewRequired(
                        event.getRecipientEmail(), event.getRecipientName(), reason);
                default -> {
                    log.warn("[RECRUITER] Type non reconnu: {}", eventType);
                    return;
                }
            }

            if (event.getEventId() != null) {
                idempotenceService.markAsProcessed(event.getEventId(), eventType, event.getRecipientEmail());
            }
            log.info("[✅ RECRUITER.{}] Email envoyé à {}", eventType, event.getRecipientEmail());
        } catch (Exception e) {
            log.error("[❌ RECRUITER] Erreur: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement décision recruteur", e);
        }
    }
}
