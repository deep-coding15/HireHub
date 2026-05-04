package com.hirehub.event.service;

import com.hirehub.event.entity.EventLog;
import com.hirehub.event.repository.EventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Persiste un événement dans le log d'audit
     */
    public EventLog logEvent(String eventId, String eventType, String message,
                             String sourceService, String destinationService) {
        try {
            EventLog eventLog = EventLog.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .sourceService(sourceService)
                    .destinationService(destinationService)
                    .status("SUCCESS")
                    .build();

            EventLog savedLog = eventLogRepository.save(eventLog);
            log.debug("[EventLog] Événement persisté: {} ({})", eventType, eventId);
            return savedLog;

        } catch (Exception e) {
            log.error("[EventLog ERROR] Erreur lors de la persistance: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur persistance EventLog", e);
        }
    }

    /**
     * Persiste un événement avec un statut d'erreur
     */
    public EventLog logEventError(String eventId, String eventType, String message,
                                  String sourceService, String errorMessage) {
        try {
            EventLog eventLog = EventLog.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .sourceService(sourceService)
                    .status("FAILED")
                    .errorMessage(errorMessage)
                    .build();

            return eventLogRepository.save(eventLog);

        } catch (Exception e) {
            log.error("[EventLog ERROR] Erreur lors du log d'erreur: {}", e.getMessage(), e);
            // Ne pas relancer - on ne veut pas bloquer l'audit
            return null;
        }
    }

    /**
     * Récupère tous les logs d'événements
     */
    public List<EventLog> getAllEventLogs() {
        log.debug("[EventLog] Récupération de tous les logs");
        return eventLogRepository.findAll();
    }

    /**
     * Récupère un log par ID
     */
    public EventLog getEventLogById(Long id) {
        log.debug("[EventLog] Récupération du log: {}", id);
        return eventLogRepository.findById(id).orElse(null);
    }

    /**
     * Récupère les logs par type d'événement
     */
    public List<EventLog> getEventLogsByType(String eventType) {
        log.debug("[EventLog] Recherche des logs par type: {}", eventType);
        return eventLogRepository.findByEventType(eventType);
    }

    /**
     * Récupère les logs par service source
     */
    public List<EventLog> getEventLogsBySourceService(String sourceService) {
        log.debug("[EventLog] Recherche des logs par service source: {}", sourceService);
        return eventLogRepository.findBySourceService(sourceService);
    }

    /**
     * Récupère les logs par service destination
     */
    public List<EventLog> getEventLogsByDestinationService(String destinationService) {
        log.debug("[EventLog] Recherche des logs par service destination: {}", destinationService);
        return eventLogRepository.findByDestinationService(destinationService);
    }

    /**
     * Récupère les logs par statut
     */
    public List<EventLog> getEventLogsByStatus(String status) {
        log.debug("[EventLog] Recherche des logs par statut: {}", status);
        return eventLogRepository.findByStatus(status);
    }

    /**
     * Filtre les logs par type et statut
     */
    public List<EventLog> filterEventLogs(String eventType, String status) {
        log.debug("[EventLog] Filtrage des logs - Type: {}, Statut: {}", eventType, status);

        if (eventType != null && status != null) {
            return eventLogRepository.findByEventTypeAndStatus(eventType, status);
        } else if (eventType != null) {
            return getEventLogsByType(eventType);
        } else if (status != null) {
            return getEventLogsByStatus(status);
        }
        return getAllEventLogs();
    }

    /**
     * Sauvegarde un log d'événement
     */
    public EventLog saveEventLog(EventLog eventLog) {
        try {
            if (eventLog.getCreatedAt() == null) {
                eventLog.setCreatedAt(LocalDateTime.now());
            }
            EventLog savedLog = eventLogRepository.save(eventLog);
            log.info("[EventLog] Log sauvegardé avec succès: {}", savedLog.getId());
            return savedLog;
        } catch (Exception e) {
            log.error("[EventLog ERROR] Erreur lors de la sauvegarde du log: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur sauvegarde EventLog", e);
        }
    }

    /**
     * Met à jour un log d'événement
     */
    public EventLog updateEventLog(Long id, EventLog eventLog) {
        try {
            return eventLogRepository.findById(id)
                    .map(existingLog -> {
                        if (eventLog.getEventType() != null) {
                            existingLog.setEventType(eventLog.getEventType());
                        }
                        if (eventLog.getMessage() != null) {
                            existingLog.setMessage(eventLog.getMessage());
                        }
                        if (eventLog.getStatus() != null) {
                            existingLog.setStatus(eventLog.getStatus());
                        }
                        if (eventLog.getErrorMessage() != null) {
                            existingLog.setErrorMessage(eventLog.getErrorMessage());
                        }
                        EventLog updatedLog = eventLogRepository.save(existingLog);
                        log.info("[EventLog] Log mis à jour avec succès: {}", id);
                        return updatedLog;
                    })
                    .orElse(null);
        } catch (Exception e) {
            log.error("[EventLog ERROR] Erreur lors de la mise à jour du log: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur mise à jour EventLog", e);
        }
    }

    /**
     * Supprime un log d'événement
     */
    public void deleteEventLog(Long id) {
        try {
            eventLogRepository.deleteById(id);
            log.info("[EventLog] Log supprimé avec succès: {}", id);
        } catch (Exception e) {
            log.error("[EventLog ERROR] Erreur lors de la suppression du log: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur suppression EventLog", e);
        }
    }

    /**
     * Compte les logs par statut
     */
    public long countByStatus(String status) {
        return eventLogRepository.countByStatus(status);
    }

    /**
     * Récupère les statistiques d'événements
     */
    public Map<String, Long> getEventStatistics() {
        log.debug("[EventLog] Calcul des statistiques d'événements");
        return Map.of(
                "SUCCESS", countByStatus("SUCCESS"),
                "FAILED", countByStatus("FAILED"),
                "PENDING", countByStatus("PENDING"),
                "TOTAL", eventLogRepository.count()
        );
    }
}
