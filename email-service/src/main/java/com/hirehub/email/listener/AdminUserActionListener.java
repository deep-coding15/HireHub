package com.hirehub.email.listener;

import com.hirehub.common.constants.EventType;
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
public class AdminUserActionListener {

    private final EmailBusinessServiceImpl emailService;
    private final IdempotenceService idempotenceService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER)
    public void handleAdminUserAction(@Payload EmailEventDTO event) {
        try {
            if (event.getEventId() != null && idempotenceService.isAlreadyProcessed(event.getEventId())) {
                log.warn("[ADMIN.USER] Événement {} déjà traité", event.getEventId());
                return;
            }

            String action = event.getPayload() != null
                    ? String.valueOf(event.getPayload().getOrDefault("action", "UNKNOWN"))
                    : "UNKNOWN";
            String role = event.getPayload() != null
                    ? String.valueOf(event.getPayload().getOrDefault("role", ""))
                    : "";

            log.info("[ADMIN.USER] {} pour {}", action, event.getRecipientEmail());

            emailService.sendAdminUserAction(
                    event.getRecipientEmail(),
                    event.getRecipientName(),
                    action,
                    role
            );

            if (event.getEventId() != null) {
                idempotenceService.markAsProcessed(
                        event.getEventId(),
                        EventType.ADMIN_USER_ACTION,
                        event.getRecipientEmail()
                );
            }
            log.info("[✅ ADMIN.USER] Email envoyé à {}", event.getRecipientEmail());
        } catch (Exception e) {
            log.error("[❌ ADMIN.USER] Erreur: {}", e.getMessage(), e);
            if (event.getEventId() != null) {
                idempotenceService.markAsFailed(
                        event.getEventId(),
                        EventType.ADMIN_USER_ACTION,
                        event.getRecipientEmail(),
                        e.getMessage()
                );
            }
            throw new RuntimeException("Erreur traitement action admin", e);
        }
    }
}
