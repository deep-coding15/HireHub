package com.hirehub.event.web;

import com.hirehub.event.entity.EventLog;
import com.hirehub.event.service.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des logs d'événements
 * Endpoints pour consulter, créer, modifier et supprimer des logs d'audit
 */
@RestController
@RequestMapping("/api/event-logs")
@RequiredArgsConstructor
@Slf4j
public class EventLogController {

    private final EventLogService eventLogService;

    /**
     * GET /api/event-logs
     * Récupère tous les logs d'événements
     */
    @GetMapping
    public ResponseEntity<List<EventLog>> getAllEventLogs() {
        log.info("[EventLogController] Récupération de tous les logs");
        List<EventLog> logs = eventLogService.getAllEventLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/event-logs/{id}
     * Récupère un log par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventLog> getEventLogById(@PathVariable Long id) {
        log.info("[EventLogController] Récupération du log: {}", id);
        EventLog logg = eventLogService.getEventLogById(id);
        if (logg != null) {
            return ResponseEntity.ok(logg);
        }
        log.warn("[EventLogController] Log non trouvé: {}", id);
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/event-logs/by-type/{eventType}
     * Récupère les logs par type d'événement
     */
    @GetMapping("/by-type/{eventType}")
    public ResponseEntity<List<EventLog>> getEventLogsByType(@PathVariable String eventType) {
        log.info("[EventLogController] Récupération des logs par type: {}", eventType);
        List<EventLog> logs = eventLogService.getEventLogsByType(eventType);
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/event-logs/by-source/{sourceService}
     * Récupère les logs par service source
     */
    @GetMapping("/by-source/{sourceService}")
    public ResponseEntity<List<EventLog>> getEventLogsBySourceService(@PathVariable String sourceService) {
        log.info("[EventLogController] Récupération des logs par service source: {}", sourceService);
        List<EventLog> logs = eventLogService.getEventLogsBySourceService(sourceService);
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/event-logs/by-destination/{destinationService}
     * Récupère les logs par service destination
     */
    @GetMapping("/by-destination/{destinationService}")
    public ResponseEntity<List<EventLog>> getEventLogsByDestinationService(@PathVariable String destinationService) {
        log.info("[EventLogController] Récupération des logs par service destination: {}", destinationService);
        List<EventLog> logs = eventLogService.getEventLogsByDestinationService(destinationService);
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/event-logs/by-status/{status}
     * Récupère les logs par statut
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<EventLog>> getEventLogsByStatus(@PathVariable String status) {
        log.info("[EventLogController] Récupération des logs par statut: {}", status);
        List<EventLog> logs = eventLogService.getEventLogsByStatus(status);
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/event-logs/filter
     * Filtre les logs par type et/ou statut
     * Paramètres: ?eventType=XXX&status=SUCCESS
     */
    @GetMapping("/filter")
    public ResponseEntity<List<EventLog>> filterEventLogs(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status) {
        log.info("[EventLogController] Filtrage des logs - Type: {}, Statut: {}", eventType, status);
        List<EventLog> logs = eventLogService.filterEventLogs(eventType, status);
        return ResponseEntity.ok(logs);
    }

    /**
     * POST /api/event-logs
     * Crée un nouveau log d'événement
     */
    @PostMapping
    public ResponseEntity<EventLog> createEventLog(@RequestBody EventLog eventLog) {
        log.info("[EventLogController] Création d'un nouveau log: {}", eventLog.getEventType());
        try {
            EventLog createdLog = eventLogService.saveEventLog(eventLog);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLog);
        } catch (Exception e) {
            log.error("[EventLogController] Erreur lors de la création du log", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/event-logs/{id}
     * Met à jour un log d'événement
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventLog> updateEventLog(@PathVariable Long id, @RequestBody EventLog eventLog) {
        log.info("[EventLogController] Mise à jour du log: {}", id);
        try {
            EventLog updatedLog = eventLogService.updateEventLog(id, eventLog);
            if (updatedLog != null) {
                return ResponseEntity.ok(updatedLog);
            }
            log.warn("[EventLogController] Log non trouvé pour mise à jour: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("[EventLogController] Erreur lors de la mise à jour du log", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/event-logs/{id}
     * Supprime un log d'événement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventLog(@PathVariable Long id) {
        log.info("[EventLogController] Suppression du log: {}", id);
        try {
            eventLogService.deleteEventLog(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("[EventLogController] Erreur lors de la suppression du log", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/event-logs/statistics
     * Récupère les statistiques d'événements
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getEventStatistics() {
        log.info("[EventLogController] Récupération des statistiques");
        try {
            Map<String, Long> statistics = eventLogService.getEventStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("[EventLogController] Erreur lors de la récupération des statistiques", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/event-logs/health
     * Endpoint de santé du service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.debug("[EventLogController] Health check");
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "event-service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
