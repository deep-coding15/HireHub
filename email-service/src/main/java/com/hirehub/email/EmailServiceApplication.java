package com.hirehub.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Service d'envoi d'e-mails (rôle métier « notification-service »).
 * Consomme les événements RabbitMQ définis dans {@link com.hirehub.common.notification.RabbitMQConstants}.
 */
@EnableRabbit
@EnableFeignClients
@ComponentScan(basePackages = {"com.hirehub.email", "com.hirehub.common"})
@SpringBootApplication
public class EmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }

}
