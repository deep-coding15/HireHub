package com.hirehub.event.repository;

import com.hirehub.event.entity.EventAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventAudit, Long> {

    EventAudit findByEventId(String eventId);

    List<EventAudit> findByEventType(String eventType);

    List<EventAudit> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<EventAudit> findByStatus(String status);

    List<EventAudit> findBySourceService(String sourceService);

    List<EventAudit> findByDestinationService(String destinationService);

    List<EventAudit> findByEventTypeAndStatus(String eventType, String status);

    long countByStatus(String status);
}
