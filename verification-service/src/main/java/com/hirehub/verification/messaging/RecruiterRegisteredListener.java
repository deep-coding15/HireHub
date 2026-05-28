package com.hirehub.verification.messaging;

import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.common.events.RecruiterRegisteredEvent;
import com.hirehub.verification.service.RecruiterDocumentVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecruiterRegisteredListener {

    private final RecruiterDocumentVerificationService recruiterDocumentVerificationService;

    public RecruiterRegisteredListener(RecruiterDocumentVerificationService recruiterDocumentVerificationService) {
        this.recruiterDocumentVerificationService = recruiterDocumentVerificationService;
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_VERIFICATION_RECRUITER)
    public void handleRecruiterRegistered(RecruiterRegisteredEvent event) {
        log.info("[VERIFICATION] Demande de verification recue pour userId={}", event.getUserId());
        try {
            recruiterDocumentVerificationService.verifyAsync(event);
            log.info("[VERIFICATION] Verification lancee pour userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("[VERIFICATION] Erreur lors du lancement de la verification pour userId={}", event.getUserId(), e);
            throw e;
        }
    }
}
