package com.hirehub.entretien.services;

import com.hirehub.common.events.EntretienPlanifiedEvent;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.entretien.entities.Entretien;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EntretienNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EntretienNotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Entretien entretien, boolean annule) {
        EntretienPlanifiedEvent event = new EntretienPlanifiedEvent();
        event.setEntretienId(entretien.getId());
        event.setCandidatureId(entretien.getCandidatureId());
        event.setCandidatId(entretien.getCandidatId());
        event.setDateHeure(entretien.getDateHeure());
        event.setLieu(entretien.getLieu());
        event.setLienVisio(entretien.getLienVisio());
        event.setAnnule(annule);
        event.setCorrelationId(MDC.get("correlationId"));

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE,
                event
        );
        log.info("[ENTRETIEN] Event publié — id={} annule={}", entretien.getId(), annule);
    }
}