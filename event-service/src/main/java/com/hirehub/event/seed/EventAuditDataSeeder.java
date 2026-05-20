package com.hirehub.event.seed;

import com.hirehub.event.entity.EventAudit;
import com.hirehub.event.repository.EventLogRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventAuditDataSeeder implements ApplicationRunner {

    private final EventLogRepository eventLogRepository;

    public EventAuditDataSeeder(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    public void seedData() {
        if (eventLogRepository.count() == 0) {
            EventAudit event1 = EventAudit.builder()
                    .eventId("event-1")
                    .eventType("CANDIDATURE.CREATED")
                    .message("Candidature created successfully.")
                    .createdAt(LocalDateTime.now())
                    .sourceService("candidature-service")
                    .destinationService("notification-service")
                    .status("SUCCESS")
                    .build();

            EventAudit event2 = EventAudit.builder()
                    .eventId("event-2")
                    .eventType("CANDIDATURE.UPDATED")
                    .message("Candidature updated successfully.")
                    .createdAt(LocalDateTime.now())
                    .sourceService("candidature-service")
                    .destinationService("notification-service")
                    .status("SUCCESS")
                    .build();

            eventLogRepository.save(event1);
            eventLogRepository.save(event2);

            System.out.println("EventAuditDataSeeder: Seeded initial data into event_audit_log table.");
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedData();
    }
}
