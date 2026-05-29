package com.hirehub.frontend.notification;

import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.common.notification.RabbitMQConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class FrontendEmailEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public FrontendEmailEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String eventType, String email, String name, String routingKey, Map<String, Object> payload) {
        EmailEventDTO event = new EmailEventDTO();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setRecipientEmail(email);
        event.setRecipientName(name);
        event.setPayload(payload);
        event.setTimestamp(System.currentTimeMillis());
        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, routingKey, event);
    }
}
