package com.hirehub.email.controller;

import com.hirehub.common.dtos.candidatures.CandidatureDTO;
import com.hirehub.common.dtos.candidatures.CandidatureStatutChangedDTO;
import com.hirehub.common.dtos.entretiens.EntretienPlanifiedDTO;
import com.hirehub.email.email.interfaces.EmailBusinessService;
import jakarta.validation.Valid;
import com.hirehub.email.EmailBusinessServiceImpl;
import com.hirehub.email.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification-service/notifications")
@Validated
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);
    private final EmailBusinessServiceImpl businessMailService;

    public EmailController(EmailBusinessServiceImpl businessMailService) {
      this.businessMailService = businessMailService;
    }

    /*@PostMapping("/envoyer")
    public ResponseEntity<String> envoyerEmail(
            @RequestParam String typeEmail,
            @RequestBody Ca
            ){

    }*/

    @PostMapping("/candidature-confirmation")
    public ResponseEntity<Void> sendCandidatureConfirmation(@Valid @RequestBody CandidatureDTO candidature) {
        log.info("POST /candidature-confirmation payload received: candidatureId={}", candidature != null ? candidature.getId() : "null");

        if (candidature == null) {
            log.warn("Invalid candidature-confirmation request: body is null");
            return ResponseEntity.badRequest().build();
        }

        // For confirmation we expect at least the candidature id and candidatId/offreId to be present
        if (Utils.isBlank(candidature.getCandidatId()) || Utils.isBlank(candidature.getOffreId())) {
            log.warn("Invalid candidature-confirmation payload: missing candidatId/offreId for candidatureId={}", candidature.getId());
            return ResponseEntity.badRequest().build();
        }

        try {
            businessMailService.sendCandidatureConfirmation(candidature);
            log.info("Candidature confirmation email enqueued for candidatureId={}", candidature.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error while sending candidature confirmation for id={}: {}", candidature.getId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
/*
* public class CandidatureDTO {
    private String id;
    private String candidatId;
    private String offreId;
    private String status;
    private String cvPath;
    private String lettreMotivationPath;
}*/
    @PostMapping("/statut-changed")
    public ResponseEntity<Void> sendStatutChangedNotification(@Valid @RequestBody CandidatureStatutChangedDTO candidatureChangedDTO){
        log.info("POST /statut-changed payload received for candidatureId={}", candidatureChangedDTO != null ? candidatureChangedDTO.getCandidatureId() : "null");

        if (candidatureChangedDTO == null) {
            log.warn("Invalid statut-changed request: body is null");
            return ResponseEntity.badRequest().build();
        }

        if (Utils.isBlank(candidatureChangedDTO.getCandidatEmail())
                || Utils.isBlank(candidatureChangedDTO.getOffreTitle()) || Utils.isBlank(candidatureChangedDTO.getCandidatName())
                || Utils.isBlank(candidatureChangedDTO.getAncienStatut()) || Utils.isBlank(candidatureChangedDTO.getNouveauStatut())) {
            log.warn("Invalid statut-changed payload: missing required fields for candidatureId={}", candidatureChangedDTO.getCandidatureId());
            return ResponseEntity.badRequest().build();
        }

        try {
            businessMailService.sendCandidatureStatutChangedNotification(
                    candidatureChangedDTO.getCandidatEmail(),
                    candidatureChangedDTO.getCandidatName(),
                    candidatureChangedDTO.getOffreTitle(),
                    candidatureChangedDTO.getAncienStatut(),
                    candidatureChangedDTO.getNouveauStatut(),
                    candidatureChangedDTO.getCommentaire()
            );
            log.info("Statut changed email enqueued for candidatureId={}", candidatureChangedDTO.getCandidatureId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error while sending statut-changed email for candidatureId={}: {}", candidatureChangedDTO.getCandidatureId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/entretien-planification")
    public ResponseEntity<Void> sendEntretienPlanification(@Valid @RequestBody EntretienPlanifiedDTO entretienPlanifiedDTO){
        log.info("POST /entretien-planification payload received for candidatureId={}", entretienPlanifiedDTO != null ? entretienPlanifiedDTO.getCandidatureId() : "null");

        if (entretienPlanifiedDTO == null) {
            log.warn("Invalid entretien-planification request: body is null");
            return ResponseEntity.badRequest().build();
        }

        if (Utils.isBlank(entretienPlanifiedDTO.getCandidatEmail()) || Utils.isBlank(entretienPlanifiedDTO.getLieuEntretien())
                || Utils.isBlank(entretienPlanifiedDTO.getOffreTitle()) || Utils.isBlank(entretienPlanifiedDTO.getCandidatName())
                || Utils.isBlank(entretienPlanifiedDTO.getDateEntretien()) || Utils.isBlank(entretienPlanifiedDTO.getHeureEntretien())) {
            log.warn("Invalid entretien-planification payload: missing required fields for candidatureId={}", entretienPlanifiedDTO.getCandidatureId());
            return ResponseEntity.badRequest().build();
        }

        try {
            businessMailService.sendEntretienPlanification(
                    entretienPlanifiedDTO.getCandidatEmail(),
                    entretienPlanifiedDTO.getCandidatName(),
                    entretienPlanifiedDTO.getOffreTitle(),
                    entretienPlanifiedDTO.getDateEntretien(),
                    entretienPlanifiedDTO.getLieuEntretien(),
                    entretienPlanifiedDTO.getInterviewerName()
            );
            log.info("Entretien planification email enqueued for candidatureId={}", entretienPlanifiedDTO.getCandidatureId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error while sending entretien-planification email for candidatureId={}: {}", entretienPlanifiedDTO.getCandidatureId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
