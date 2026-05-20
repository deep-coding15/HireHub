package com.hirehub.email.seed;

import com.hirehub.email.entity.EmailEventProcessed;
import com.hirehub.email.repository.EmailEventProcessedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
@Order(200)
@Profile("!test")
public class EmailEventsProcessedSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmailEventsProcessedSeeder.class);

    private final EmailEventProcessedRepository repository;

    public EmailEventsProcessedSeeder(EmailEventProcessedRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            long count = repository.count();
            if (count > 0) {
                log.info("EmailEventsProcessedSeeder: {} existing records found, skipping seeder.", count);
                return;
            }

            log.info("EmailEventsProcessedSeeder: no records found, seeding a sample processed event...");

            EmailEventProcessed sample = EmailEventProcessed.builder()
                    .eventId("sample-event-1")
                    .eventType("CANDIDATURE.CREATED")
                    .recipientEmail("candidate@example.com")
                    .status("SUCCESS")
                    .processedAt(LocalDateTime.now())
                    .errorMessage(null)
                    .retryCount(0)
                    .build();

            repository.saveAll(Collections.singletonList(sample));

            log.info("EmailEventsProcessedSeeder: inserted sample processed event.");
        } catch (Exception e) {
            log.error("EmailEventsProcessedSeeder: error while seeding data", e);
        }
    }
}

