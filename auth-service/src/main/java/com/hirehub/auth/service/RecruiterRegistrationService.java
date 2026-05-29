package com.hirehub.auth.service;

import com.hirehub.auth.dto.RegisterRecruiterForm;
import com.hirehub.auth.messaging.AuthEmailEventPublisher;
import com.hirehub.auth.model.RecruiterRegistrationStatusSnapshot;
import com.hirehub.auth.model.UserAccount;
import com.hirehub.auth.repo.UserAccountRepository;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.common.enums.RecruiterVerificationStatus;
import com.hirehub.common.events.RecruiterRegisteredEvent;
import com.hirehub.common.events.RecruiterVerifiedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecruiterRegistrationService {

    private final RabbitTemplate rabbitTemplate;
    private final UserRegistrationService userRegistrationService;
    private final UserAccountRepository userAccountRepository;
    private final AuthEmailEventPublisher authEmailEventPublisher;
    private final Map<String, RecruiterRegistrationStatusSnapshot> statuses = new ConcurrentHashMap<>();

    public RecruiterRegistrationService(
            RabbitTemplate rabbitTemplate,
            UserRegistrationService userRegistrationService,
            UserAccountRepository userAccountRepository,
            AuthEmailEventPublisher authEmailEventPublisher
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.userRegistrationService = userRegistrationService;
        this.userAccountRepository = userAccountRepository;
        this.authEmailEventPublisher = authEmailEventPublisher;
    }

    public void savePendingRegistration(String userId, RegisterRecruiterForm form, String justificatifNom) {
        statuses.put(
                userId,
                new RecruiterRegistrationStatusSnapshot(
                        userId,
                        form.getEmail(),
                        RecruiterVerificationStatus.PENDING_AUTO_CHECK,
                        null,
                        "Verification OCR/API en cours pour le document: " + justificatifNom,
                        "VERIFICATION_SERVICE_PENDING"
                )
        );
    }

    public void publishRegisteredEvent(RecruiterRegisteredEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.ROUTING_RECRUITER_REGISTERED,
                event
        );
    }

    public void applyVerificationResult(RecruiterVerifiedEvent event) {
        RecruiterVerificationStatus status = RecruiterVerificationStatus.valueOf(event.getVerificationStatus());
        userRegistrationService.applyRecruiterVerification(UUID.fromString(event.getUserId()), status);

        RecruiterRegistrationStatusSnapshot previous = statuses.get(event.getUserId());
        String email = previous != null ? previous.email() : null;

        statuses.put(
                event.getUserId(),
                new RecruiterRegistrationStatusSnapshot(
                        event.getUserId(),
                        email,
                        status,
                        event.getVerificationScore(),
                        event.getDecisionMessage(),
                        event.getVerificationSource()
                )
        );

        publishRecruiterDecisionEmail(event, status);
    }

    private void publishRecruiterDecisionEmail(RecruiterVerifiedEvent event, RecruiterVerificationStatus status) {
        try {
            UserAccount user = userAccountRepository.findById(UUID.fromString(event.getUserId())).orElse(null);
            if (user == null) {
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", event.getUserId());
            payload.put("decisionMessage", event.getDecisionMessage());
            payload.put("verificationScore", event.getVerificationScore());

            switch (status) {
                case APPROVED -> authEmailEventPublisher.publish(
                        "RECRUITER.APPROVED",
                        user.getEmail(),
                        user.getFullName(),
                        RabbitMQConstants.ROUTING_RECRUITER_APPROVED,
                        payload
                );
                case REJECTED -> authEmailEventPublisher.publish(
                        "RECRUITER.REJECTED",
                        user.getEmail(),
                        user.getFullName(),
                        RabbitMQConstants.ROUTING_RECRUITER_REJECTED,
                        payload
                );
                case REVIEW_REQUIRED -> authEmailEventPublisher.publish(
                        "RECRUITER.REVIEW_REQUIRED",
                        user.getEmail(),
                        user.getFullName(),
                        RabbitMQConstants.ROUTING_RECRUITER_REJECTED,
                        payload
                );
                default -> { }
            }
        } catch (Exception ignored) {
            // Best-effort notification
        }
    }

    public Optional<RecruiterRegistrationStatusSnapshot> getStatus(String userId) {
        return Optional.ofNullable(statuses.get(userId));
    }
}
