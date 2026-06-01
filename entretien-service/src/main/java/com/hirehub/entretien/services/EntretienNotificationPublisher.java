package com.hirehub.entretien.services;

import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.entretien.entities.Entretien;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class EntretienNotificationPublisher {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final RabbitTemplate rabbitTemplate;

    public EntretienNotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publie un événement entretien planifié/annulé au format EmailEventDTO
     * (attendu par EntretienPlanifieListenerImpl dans email-service).
     *
     * @param entretien   l'entretien persisté
     * @param candidatEmail email du candidat (fourni par le frontend, non persisté)
     * @param candidatNom   nom du candidat pour personnalisation
     * @param offreTitre    titre de l'offre
     * @param annule        true si annulation
     */
    public void publish(Entretien entretien,
                        String candidatEmail,
                        String candidatNom,
                        String offreTitre,
                        boolean annule) {

        if (candidatEmail == null || candidatEmail.isBlank()) {
            log.warn("[ENTRETIEN] Email candidat absent — notification ignorée pour entretienId={}",
                    entretien.getId());
            return;
        }

        String displayName = (candidatNom != null && !candidatNom.isBlank())
                ? candidatNom : candidatEmail;
        String titre       = (offreTitre  != null && !offreTitre.isBlank())
                ? offreTitre : "poste";

        Map<String, Object> payload = new HashMap<>();
        payload.put("entretienId",        entretien.getId());
        payload.put("candidatureId",      entretien.getCandidatureId());
        payload.put("offerTitle",         titre);
        payload.put("interviewDate",      entretien.getDateHeure() != null
                ? entretien.getDateHeure().format(DATE_FMT) : "");
        payload.put("interviewLocation",  buildLocation(entretien));
        payload.put("interviewType",      entretien.getType() != null
                ? entretien.getType().name() : "");
        payload.put("lienVisio",          entretien.getLienVisio() != null
                ? entretien.getLienVisio() : "");
        payload.put("consignes",          entretien.getNotesInternes() != null
                ? entretien.getNotesInternes() : "");
        payload.put("annule",             annule);
        if (annule) {
            payload.put("comment", "Votre entretien a été annulé par le recruteur.");
        }

        String eventType   = annule ? "ENTRETIEN_ANNULE" : "ENTRETIEN_PLANIFIE";
        String routingKey  = RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE;

        EmailEventDTO event = new EmailEventDTO();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setRecipientEmail(candidatEmail);
        event.setRecipientName(displayName);
        event.setPayload(payload);
        event.setTimestamp(System.currentTimeMillis());
        event.setCorrelationId(MDC.get("correlationId"));

        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, routingKey, event);
        log.info("[ENTRETIEN] Event '{}' publié → {} (candidatEmail={})",
                eventType, entretien.getId(), candidatEmail);
    }

    /** Rétrocompatibilité : appelé par cancel() qui n'a pas les infos email */
    public void publish(Entretien entretien, boolean annule) {
        log.warn("[ENTRETIEN] publish() sans email appelé pour entretienId={} — notification ignorée",
                entretien.getId());
    }

    private String buildLocation(Entretien e) {
        if (e.getLienVisio() != null && !e.getLienVisio().isBlank()) {
            return e.getLienVisio();
        }
        if (e.getLieu() != null && !e.getLieu().isBlank()) {
            return e.getLieu();
        }
        return "Entretien téléphonique";
    }
}
