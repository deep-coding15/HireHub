package com.hirehub.event.service;

import com.hirehub.event.entity.EventAudit;
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
    public EventAudit logEvent(String eventId, String eventType, String message,
                               String sourceService, String destinationService) {
        try {
            EventAudit eventAudit = EventAudit.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .sourceService(sourceService)
                    .destinationService(destinationService)
                    .status("SUCCESS")
                    .build();

            EventAudit savedLog = eventLogRepository.save(eventAudit);
            log.debug("[EventAudit] Événement persisté: {} ({})", eventType, eventId);
            return savedLog;

        } catch (Exception e) {
            log.error("[EventAudit ERROR] Erreur lors de la persistance: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur persistance EventAudit", e);
        }
    }

    /**
     * Persiste un événement avec un statut d'erreur
     */
    public EventAudit logEventError(String eventId, String eventType, String message,
                                    String sourceService, String errorMessage) {
        try {
            EventAudit eventAudit = EventAudit.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .sourceService(sourceService)
                    .status("FAILED")
                    .errorMessage(errorMessage)
                    .build();

            return eventLogRepository.save(eventAudit);

        } catch (Exception e) {
            log.error("[EventAudit ERROR] Erreur lors du log d'erreur: {}", e.getMessage(), e);
            // Ne pas relancer - on ne veut pas bloquer l'audit
            return null;
        }
    }

    /**
     * Récupère tous les logs d'événements
     */
    public List<EventAudit> getAllEventLogs() {
        log.debug("[EventAudit] Récupération de tous les logs");
        return eventLogRepository.findAll();
    }

    /**
     * Récupère un log par ID
     */
    public EventAudit getEventLogById(Long id) {
        log.debug("[EventAudit] Récupération du log: {}", id);
        return eventLogRepository.findById(id).orElse(null);
    }

    /**
     * Récupère les logs par type d'événement
     */
    public List<EventAudit> getEventLogsByType(String eventType) {
        log.debug("[EventAudit] Recherche des logs par type: {}", eventType);
        return eventLogRepository.findByEventType(eventType);
    }

    /**
     * Récupère les logs par service source
     */
    public List<EventAudit> getEventLogsBySourceService(String sourceService) {
        log.debug("[EventAudit] Recherche des logs par service source: {}", sourceService);
        return eventLogRepository.findBySourceService(sourceService);
    }

    /**
     * Récupère les logs par service destination
     */
    public List<EventAudit> getEventLogsByDestinationService(String destinationService) {
        log.debug("[EventAudit] Recherche des logs par service destination: {}", destinationService);
        return eventLogRepository.findByDestinationService(destinationService);
    }

    /**
     * Récupère les logs par statut
     */
    public List<EventAudit> getEventLogsByStatus(String status) {
        log.debug("[EventAudit] Recherche des logs par statut: {}", status);
        return eventLogRepository.findByStatus(status);
    }

    /**
     * Filtre les logs par type et statut
     */
    public List<EventAudit> filterEventLogs(String eventType, String status) {
        log.debug("[EventAudit] Filtrage des logs - Type: {}, Statut: {}", eventType, status);

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
    public EventAudit saveEventLog(EventAudit eventAudit) {
        try {
            if (eventAudit.getCreatedAt() == null) {
                eventAudit.setCreatedAt(LocalDateTime.now());
            }
            EventAudit savedLog = eventLogRepository.save(eventAudit);
            log.info("[EventAudit] Log sauvegardé avec succès: {}", savedLog.getId());
            return savedLog;
        } catch (Exception e) {
            log.error("[EventAudit ERROR] Erreur lors de la sauvegarde du log: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur sauvegarde EventAudit", e);
        }
    }

    /**
     * Met à jour un log d'événement
     */
    public EventAudit updateEventLog(Long id, EventAudit eventAudit) {
        try {
            return eventLogRepository.findById(id)
                    .map(existingLog -> {
                        if (eventAudit.getEventType() != null) {
                            existingLog.setEventType(eventAudit.getEventType());
                        }
                        if (eventAudit.getMessage() != null) {
                            existingLog.setMessage(eventAudit.getMessage());
                        }
                        if (eventAudit.getStatus() != null) {
                            existingLog.setStatus(eventAudit.getStatus());
                        }
                        if (eventAudit.getErrorMessage() != null) {
                            existingLog.setErrorMessage(eventAudit.getErrorMessage());
                        }
                        EventAudit updatedLog = eventLogRepository.save(existingLog);
                        log.info("[EventAudit] Log mis à jour avec succès: {}", id);
                        return updatedLog;
                    })
                    .orElse(null);
        } catch (Exception e) {
            log.error("[EventAudit ERROR] Erreur lors de la mise à jour du log: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur mise à jour EventAudit", e);
        }
    }

    /**
     * Supprime un log d'événement
     */
    public void deleteEventLog(Long id) {
        try {
            eventLogRepository.deleteById(id);
            log.info("[EventAudit] Log supprimé avec succès: {}", id);
        } catch (Exception e) {
            log.error("[EventAudit ERROR] Erreur lors de la suppression du log: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur suppression EventAudit", e);
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
        log.debug("[EventAudit] Calcul des statistiques d'événements");
        return Map.of(
                "SUCCESS", countByStatus("SUCCESS"),
                "FAILED", countByStatus("FAILED"),
                "PENDING", countByStatus("PENDING"),
                "TOTAL", eventLogRepository.count()
        );
    }
}
