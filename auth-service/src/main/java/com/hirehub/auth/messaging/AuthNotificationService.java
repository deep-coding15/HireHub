package com.hirehub.auth.messaging;

import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.auth.model.UserAccount;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthNotificationService {

    private final AuthEmailEventPublisher publisher;

    public AuthNotificationService(AuthEmailEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishRegister(UserAccount user) {
        publisher.publish(
                "AUTH.REGISTER",
                user.getEmail(),
                user.getFullName(),
                RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_REGISTER,
                Collections.emptyMap()
        );
    }
}
