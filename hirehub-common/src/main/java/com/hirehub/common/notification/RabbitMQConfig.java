package com.hirehub.common.notification;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitMQConfig {

    // ═══════════════════════════════════════════════════════════════
    // EXCHANGES
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public TopicExchange hirehubExchange() {
        log.info("[EXCHANGE] Création du TopicExchange: {}", RabbitMQConstants.EXCHANGE);
        return new TopicExchange(
            RabbitMQConstants.EXCHANGE,
            true,      // durable = survive aux redémarrages de RabbitMQ
            false      // autoDelete = ne pas supprimer si plus de queues
        );
    }

    /**
     * Dead Letter Exchange : reçoit les messages rejetés après épuisement des retry.
     * Chaque queue principale pointe vers cet exchange via x-dead-letter-exchange.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        log.info("[EXCHANGE] Création du Dead Letter Exchange: {}", RabbitMQConstants.DEAD_LETTER_EXCHANGE);
        return new DirectExchange(RabbitMQConstants.DEAD_LETTER_EXCHANGE, true, false);
    }

    // ═══════════════════════════════════════════════════════════════
    // QUEUES PRINCIPALES (avec pointeur DLX)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Queue pour les NOUVELLES CANDIDATURES
     * Événements: candidature.created
     * Consumer: NotificationService (envoie email de confirmation)
     */
    @Bean
    public Queue notificationCandidatureQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_NOTIFICATION_CANDIDATURE)
                .build();
    }

    @Bean
    public Queue auditCandidatureQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_AUDIT_CANDIDATURE);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_AUDIT_CANDIDATURE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_AUDIT_CANDIDATURE)
                .build();
    }

    /**
     * Queue pour les CHANGEMENTS DE STATUT
     * Événements: candidature.statut.changed
     * Consumer: NotificationService
     */
    @Bean
    public Queue notificationStatutQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_STATUT);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_NOTIFICATION_STATUT)
                .build();
    }

    /**
     * Queue pour les ENTRETIENS PLANIFIÉS/ANNULÉS
     * Événements: entretien.planifie
     * Consumer: NotificationService
     */
    @Bean
    public Queue notificationEntretienQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_NOTIFICATION_ENTRETIEN)
                .build();
    }

    /**
     * Queue pour les DÉCISIONS RECRUTEUR
     * Événements: recruiter.request.approved, recruiter.request.rejected
     * Consumer: NotificationService
     */
    @Bean
    public Queue notificationRecruiterQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_NOTIFICATION_RECRUITER)
                .build();
    }

    /**
     * Queue pour les ACTIONS ADMIN sur les utilisateurs
     * Événements: user.blocked, user.deleted
     * Consumer: NotificationService ou audit-service
     */
    @Bean
    public Queue notificationAdminUserQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_NOTIFICATION_ADMIN_USER)
                .build();
    }

    /**
     * Queue pour les ÉVÉNEMENTS D'AUTHENTIFICATION
     * Événements: user.authentification.register, login, logout, etc.
     * Consumer: NotificationService (emails OTP, confirmations, etc.)
     */
    @Bean
    public Queue notificationAuthenticationQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_NOTIFICATION_AUTHENTIFICATION)
                .build();
    }

    /**
     * Queue pour la VÉRIFICATION RECRUTEUR (contrôle OCR/API)
     * Événements: recruiter.registered
     * Consumer: verification-service
     */
    @Bean
    public Queue verificationRecruiterQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_VERIFICATION_RECRUITER);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_VERIFICATION_RECRUITER)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_VERIFICATION_RECRUITER)
                .build();
    }

    /**
     * Queue pour l'AUTH en réception du résultat de vérification
     * Événements: recruiter.verified
     * Consumer: auth-service
     */
    @Bean
    public Queue authRecruiterVerifiedQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_AUTH_RECRUITER_VERIFIED);
        return QueueBuilder
                .durable(RabbitMQConstants.QUEUE_AUTH_RECRUITER_VERIFIED)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_AUTH_RECRUITER_VERIFIED)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // DEAD LETTER QUEUES
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Queue dlqNotificationCandidature() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_NOTIFICATION_CANDIDATURE).build();
    }

    @Bean
    public Queue dlqAuditCandidature() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_AUDIT_CANDIDATURE).build();
    }

    @Bean
    public Queue dlqNotificationStatut() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_NOTIFICATION_STATUT).build();
    }

    @Bean
    public Queue dlqNotificationEntretien() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_NOTIFICATION_ENTRETIEN).build();
    }

    @Bean
    public Queue dlqNotificationRecruiter() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_NOTIFICATION_RECRUITER).build();
    }

    @Bean
    public Queue dlqNotificationAdminUser() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_NOTIFICATION_ADMIN_USER).build();
    }

    @Bean
    public Queue dlqNotificationAuthentification() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_NOTIFICATION_AUTHENTIFICATION).build();
    }

    @Bean
    public Queue dlqVerificationRecruiter() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_VERIFICATION_RECRUITER).build();
    }

    @Bean
    public Queue dlqAuthRecruiterVerified() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_AUTH_RECRUITER_VERIFIED).build();
    }

    // ═══════════════════════════════════════════════════════════════
    // BINDINGS DLX → DLQ
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Binding dlqNotificationCandidatureBinding() {
        return BindingBuilder
                .bind(dlqNotificationCandidature())
                .to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_NOTIFICATION_CANDIDATURE);
    }

    @Bean
    public Binding dlqAuditCandidatureBinding() {
        return BindingBuilder.bind(dlqAuditCandidature()).to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_AUDIT_CANDIDATURE);
    }

    @Bean
    public Binding dlqNotificationStatutBinding() {
        return BindingBuilder.bind(dlqNotificationStatut()).to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_NOTIFICATION_STATUT);
    }

    @Bean
    public Binding dlqNotificationEntretienBinding() {
        return BindingBuilder.bind(dlqNotificationEntretien()).to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_NOTIFICATION_ENTRETIEN);
    }

    @Bean
    public Binding dlqNotificationRecruiterBinding() {
        return BindingBuilder.bind(dlqNotificationRecruiter()).to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_NOTIFICATION_RECRUITER);
    }

    @Bean
    public Binding dlqNotificationAdminUserBinding() {
        return BindingBuilder.bind(dlqNotificationAdminUser()).to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_NOTIFICATION_ADMIN_USER);
    }

    @Bean
    public Binding dlqNotificationAuthentificationBinding() {
        return BindingBuilder.bind(dlqNotificationAuthentification()).to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_NOTIFICATION_AUTHENTIFICATION);
    }

    @Bean
    public Binding dlqVerificationRecruiterBinding() {
        return BindingBuilder.bind(dlqVerificationRecruiter()).to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_VERIFICATION_RECRUITER);
    }

    @Bean
    public Binding dlqAuthRecruiterVerifiedBinding() {
        return BindingBuilder.bind(dlqAuthRecruiterVerified()).to(deadLetterExchange())
                .with(RabbitMQConstants.DLQ_AUTH_RECRUITER_VERIFIED);
    }

    // ═══════════════════════════════════════════════════════════════
    // BINDINGS QUEUES PRINCIPALES
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Binding bindingCandidatureCreated(
            @Qualifier("notificationCandidatureQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_CANDIDATURE_CREATED);
    }

    @Bean
    public Binding bindingCandidatureCreatedAudit(
            @Qualifier("auditCandidatureQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_CANDIDATURE_CREATED);
    }

    @Bean
    public Binding bindingStatutChanged(
            @Qualifier("notificationStatutQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_CANDIDATURE_STATUT_CHANGED);
    }

    @Bean
    public Binding bindingEntretienPlanifie(
            @Qualifier("notificationEntretienQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE);
    }

    @Bean
    public Binding bindingRecruiterApproved(
            @Qualifier("notificationRecruiterQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_RECRUITER_APPROVED);
    }

    @Bean
    public Binding bindingRecruiterRejected(
            @Qualifier("notificationRecruiterQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_RECRUITER_REJECTED);
    }

    @Bean
    public Binding bindingUserBlocked(
            @Qualifier("notificationAdminUserQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_USER_BLOCKED);
    }

    @Bean
    public Binding bindingUserDeleted(
            @Qualifier("notificationAdminUserQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_USER_DELETED);
    }

    @Bean
    public Binding bindingAuthenticationRegister(
            @Qualifier("notificationAuthenticationQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_REGISTER);
    }

    @Bean
    public Binding bindingAuthenticationLogin(
            @Qualifier("notificationAuthenticationQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_LOGIN);
    }

    @Bean
    public Binding bindingAuthenticationLogout(
            @Qualifier("notificationAuthenticationQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_LOGOUT);
    }

    @Bean
    public Binding bindingRecruiterRegistered(
            @Qualifier("verificationRecruiterQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_RECRUITER_REGISTERED);
    }

    @Bean
    public Binding bindingRecruiterVerified(
            @Qualifier("authRecruiterVerifiedQueue") Queue queue,
            TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_RECRUITER_VERIFIED);
    }
}
