package com.hirehub.email.listener;

import com.hirehub.common.notification.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Consomme les messages qui ont épuisé toutes leurs tentatives de traitement.
 * Ces messages sont envoyés ici par le Dead Letter Exchange (hirehub.dlx)
 * après 3 échecs consécutifs depuis la queue principale.
 *
 * Rôle : logger l'incident pour permettre un traitement manuel ou une alerte.
 * Ne relance pas d'exception — le message doit rester en DLQ, pas reboucler.
 */
@Component
@Slf4j
public class DeadLetterConsumer {

    @RabbitListener(queues = {
        RabbitMQConstants.DLQ_NOTIFICATION_CANDIDATURE,
        RabbitMQConstants.DLQ_NOTIFICATION_STATUT,
        RabbitMQConstants.DLQ_NOTIFICATION_ENTRETIEN,
        RabbitMQConstants.DLQ_NOTIFICATION_RECRUITER,
        RabbitMQConstants.DLQ_NOTIFICATION_ADMIN_USER,
        RabbitMQConstants.DLQ_NOTIFICATION_AUTHENTIFICATION
    })
    public void handleDeadLetter(
            Message message,
            @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {

        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        Object deathCount = message.getMessageProperties().getHeaders().get("x-death");

        log.error("[DLQ] Message non traitable reçu. queue='{}' | body='{}' | x-death={}",
                queue, body, deathCount);
    }
}
