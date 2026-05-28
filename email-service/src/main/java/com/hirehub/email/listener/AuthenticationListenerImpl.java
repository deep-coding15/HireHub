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
 * Listener pour les événements d'authentification.
 * Consomme les événements RabbitMQ et envoie les emails OTP, login, logout.
 * Utilise l'idempotence pour éviter les envois en double.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationListenerImpl {

    private final EmailBusinessServiceImpl emailService;
    private final IdempotenceService idempotenceService;

    /**
     * Écoute les événements d'authentification (OTP, login, logout).
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION)
    public void handleAuthenticationEvent(@Payload EmailEventDTO event) {
        try {
            if (event.getCorrelationId() != null) {
                MDC.put("correlationId", event.getCorrelationId());
            }
            String eventId = event.getEventId();
            String eventType = event.getEventType();

            // Vérifier l'idempotence
            if (idempotenceService.isAlreadyProcessed(eventId)) {
                log.warn("[AUTH.{}] Événement {} déjà traité, abandon", eventType, eventId);
                return;
            }

            log.info("[AUTH.{}] Traitement de l'événement {} pour: {}", eventType, eventId, event.getRecipientEmail());

            switch (eventType) {
                case "AUTH.OTP":
                    handleOtpEvent(event);
                    break;
                case "AUTH.LOGIN":
                    handleLoginEvent(event);
                    break;
                case "AUTH.LOGOUT":
                    handleLogoutEvent(event);
                    break;
                default:
                    log.warn("[AUTH] Événement non reconnu: {}", eventType);
                    return;
            }

            // Marquer comme traité avec succès
            idempotenceService.markAsProcessed(eventId, eventType, event.getRecipientEmail());
            log.info("[AUTH.{}] OK - Email envoye a: {}", eventType, event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[AUTH] ERREUR lors du traitement", e);
            idempotenceService.markAsFailed(event.getEventId(), event.getEventType(), event.getRecipientEmail(), e.getMessage());
            throw new RuntimeException("Erreur traitement authentification", e);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Traite l'événement OTP (registration).
     */
    private void handleOtpEvent(EmailEventDTO event) {
        String otpCode = (String) event.getPayload().get("otpCode");
        Integer otpValidityMinutes = event.getPayload().get("otpValidityMinutes") != null
            ? ((Number) event.getPayload().get("otpValidityMinutes")).intValue()
            : 15;

        emailService.sendRegisterOtp(
                event.getRecipientEmail(),
                event.getRecipientName(),
                otpCode,
                otpValidityMinutes
        );
    }

    /**
     * Traite l'événement de connexion (login).
     */
    private void handleLoginEvent(EmailEventDTO event) {
        String loginDateTime = (String) event.getPayload().get("loginDateTime");
        String ipAddress = (String) event.getPayload().get("ipAddress");
        String userAgent = (String) event.getPayload().get("userAgent");

        emailService.sendLoginAlert(
                event.getRecipientEmail(),
                event.getRecipientName(),
                loginDateTime,
                ipAddress,
                userAgent
        );
    }

    /**
     * Traite l'événement de déconnexion (logout).
     */
    private void handleLogoutEvent(EmailEventDTO event) {
        String logoutDateTime = (String) event.getPayload().get("logoutDateTime");

        emailService.sendLogoutInfo(
                event.getRecipientEmail(),
                event.getRecipientName(),
                logoutDateTime
        );
    }

}

