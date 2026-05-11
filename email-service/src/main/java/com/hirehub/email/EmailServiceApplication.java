package com.hirehub.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.ComponentScan;

@EnableRabbit
@ComponentScan(basePackages = {"com.hirehub.email", "com.hirehub.common"})
@SpringBootApplication
public class EmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }

}
