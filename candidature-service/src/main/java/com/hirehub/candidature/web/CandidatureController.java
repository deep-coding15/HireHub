package com.hirehub.candidature.web;

import com.hirehub.candidature.config.CandidatureSecurityService;
import com.hirehub.candidature.config.RequireAuth;
import com.hirehub.candidature.config.UserContext;
import com.hirehub.candidature.dtos.CandidatureDTO;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.candidature.exceptions.CandidatureChangedStatusException;
import com.hirehub.candidature.exceptions.CandidatureUpdatedException;
import com.hirehub.candidature.exceptions.UnauthorizedException;
import com.hirehub.candidature.repository.HistoriqueStatusRepository;
import com.hirehub.candidature.services.CandidatureService;
import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour les candidatures.
 *
 * Notes:
 * - Les contrôles d'auth (candidat/recruteur) sont marqués TODO car Spring Security n'est pas encore câblé ici.
 * - Les endpoints utilisent des paramètres simples (RequestParam/PathVariable) pour rester compatibles avec le service actuel.
 */
@RestController
@RequestMapping("/candidatures")
@Slf4j
@RequireAuth
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final HistoriqueStatusRepository historiqueStatusRepository;
    private final CandidatureSecurityService securityService;

    public CandidatureController(CandidatureService candidatureService,
                                 HistoriqueStatusRepository historiqueStatusRepository,
                                 CandidatureSecurityService securityService) {
        this.candidatureService = candidatureService;
        this.historiqueStatusRepository = historiqueStatusRepository;
        this.securityService = securityService;
    }

    /**
     * POST /candidatures
     */
    @PostMapping
    @RequireAuth
    public ResponseEntity<ApiResponse<Candidature>> create(
            @RequestBody Candidature candidature) {

        candidatureService.createCandidatureByCandidat(candidature);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Candidature créée", candidature));
    }

    /**
     * GET /candidatures/moi
     */
    @GetMapping("/moi")
    public ResponseEntity<ApiResponse<List<CandidatureDTO>>> myCandidatures(HttpServletRequest request) {
        // LOG TEST
        System.out.println("AUTH HEADER: " + request.getHeader("Authorization"));

        // Affiche TOUS les headers pour débusquer celui utilisé par l'équipe Auth
        Collections.list(request.getHeaderNames())
                .forEach(h -> System.out.println(h + ": " + request.getHeader(h)));


        UserContext.UserInfo user = securityService.requireAuth();
        List<CandidatureDTO> data = candidatureService
                .getMyCandidaturesByCandidat()
                .stream().map(c -> {
                    CandidatureDTO dto = new CandidatureDTO();
                    dto.setId(c.getId());
                    dto.setCandidatId(c.getCandidatId());
                    dto.setOffreId(c.getOffreId());
                    dto.setCV_Path(c.getCV_Path());
                    dto.setLettreMotivationPath(c.getLettreMotivationPath());
                    dto.setStatus(c.getStatus());
                    dto.setDateSoumission(c.getDateSoumission());
                    dto.setDateModification(c.getDateModification());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Candidatures récupérées", data));

    }

    /**
     * GET /candidatures/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Candidature>> get(@PathVariable String id) {

        Candidature candidature = candidatureService.getCandidatureById(id);
        if (candidature == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Candidature non trouvée"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Candidature récupérée", candidature));

    }

    /**
     * PUT /candidatures/{candidatureId}/status?status=ACCEPTEE
     */
    @PutMapping("/{candidatureId}/status")
    public ResponseEntity<ApiResponse<Candidature>> updateStatus(@PathVariable String candidatureId,
                                                                 @RequestParam("status") String status) {
        try {
            // Vérifier l'authentification et les droits
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(candidatureId);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que le recruteur peut changer le statut
            securityService.requireRecruteurCanChangeStatus(user, candidature);

            // Mettre à jour le statut
            candidatureService.updateCandidatureStatusByRecruiter(candidatureId, status);
            Candidature updated = candidatureService.getCandidatureById(candidatureId);
            return ResponseEntity.ok(ApiResponse.ok("Statut mis à jour", updated));

        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
        catch (CandidatureChangedStatusException e) {
            log.warn("Statut invalide: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
        catch (Exception e) {
            log.error("Erreur update statut candidature {}: {}", candidatureId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /candidatures/{id}?cvPath=...&lettreMotivationPath=...
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Candidature>> updateFiles(@PathVariable String id,
                                                                @RequestParam(value = "cvPath", required = false) String cvPath,
                                                                @RequestParam(value = "lettreMotivationPath", required = false) String lettreMotivationPath) {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que le candidat peut mettre à jour ses fichiers
            securityService.requireCandidatCanUpdateFiles(user, candidature);

            candidatureService.updateCandidatureDetailsByCandidat(id, cvPath, lettreMotivationPath);
            Candidature updated = candidatureService.getCandidatureById(id);
            // Les fichiers sont maintenant stockés dans /app/uploads/ (volume Docker)
            // Cette logique est gérée dans le service CandidatureServiceImpl.uploadCVAndCoverLetter()
            return ResponseEntity.ok(ApiResponse.ok("Fichiers mis à jour", updated));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur update fichiers candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /candidatures/{id}/upload?cvPath=...&lettreMotivationPath=...
     *
     * Ici, on garde l'API actuelle (paths) en attendant une vraie intégration Multipart.
     */
    @PostMapping("/{id}/upload")
    public ResponseEntity<ApiResponse<Candidature>> upload(@PathVariable String id,
                                                           @RequestParam("cvPath") String cvPath,
                                                           @RequestParam("lettreMotivationPath") String lettreMotivationPath) {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que le candidat peut mettre à jour ses fichiers
            securityService.requireCandidatCanUpdateFiles(user, candidature);

            candidatureService.uploadCVAndCoverLetter(id, cvPath, lettreMotivationPath);
            Candidature updated = candidatureService.getCandidatureById(id);
            return ResponseEntity.ok(ApiResponse.ok("Upload enregistré", updated));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur upload fichiers candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /candidatures/{id}/historique
     */
    @GetMapping("/{id}/historique")
    public ResponseEntity<ApiResponse<List<HistoriqueStatus>>> historique(@PathVariable String id)
            throws Exception {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que (candidat propriétaire) OU (recruteur propriétaire de l'offre)
            if (UserRole.CANDIDAT.name().equals(user.role)) {
                securityService.requireCandidatCanViewHistory(user, candidature);
            } else if (UserRole.RECRUTEUR.name().equals(user.role)) {
                securityService.requireRecruteurCanViewHistory(user, candidature);
            } else {
                throw new UnauthorizedException("Rôle utilisateur non reconnu");
            }

            List<HistoriqueStatus> data = historiqueStatusRepository.findByCandidatureIdOrderByDateChangementDesc(id);
            return ResponseEntity.ok(ApiResponse.ok("Historique récupéré", data));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé à l'historique: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur récupération historique candidature {}: {}", id, e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * GET /candidatures/offre/{offreId}
     * Pipeline du recruteur: liste de toutes les candidatures pour une offre
     */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<ApiResponse<List<Candidature>>> byOffer(@PathVariable String offreId) {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            // Vérifier que le recruteur peut accéder au pipeline de cette offre
            securityService.requireRecruteurCanViewPipeline(user, offreId);

            List<Candidature> data = candidatureService.getCandidaturesByOfferIdByRecruiter(offreId);
            return ResponseEntity.ok(ApiResponse.ok("Candidatures récupérées", data));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur récupération candidatures offre {}: {}", offreId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /candidatures/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que le candidat peut supprimer sa candidature
            securityService.requireCandidatCanDeleteCandidature(user, candidature);

            candidatureService.deleteCandidatureByCandidat(id);
            return ResponseEntity.ok(ApiResponse.ok("Candidature supprimée", null));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur suppression candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /candidatures/{id}/download?type=cv|lettre
     *
     * Télécharge les fichiers CV ou lettre de motivation de manière sécurisée.
     * Vérifie que l'utilisateur est :
     * - Le candidat propriétaire (pour son propre CV)
     * - Un recruteur autorisé (pour voir les candidatures)
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String id,
            @RequestParam(value = "type", defaultValue = "cv") String fileType) {

        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Vérification des droits d'accès selon le rôle
            if (UserRole.CANDIDAT.name().equals(user.role)) {
                securityService.requireCandidatCanDownloadOwnFile(user, candidature);
            } else if (UserRole.RECRUTEUR.name().equals(user.role)) {
                securityService.requireRecruteurCanDownloadFile(user, candidature);
            } else {
                throw new UnauthorizedException("Rôle utilisateur non reconnu");
            }

            // Obtenir le chemin du fichier
            String filePath = fileType.equalsIgnoreCase("cv")
                ? candidature.getCV_Path()
                : candidature.getLettreMotivationPath();

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Créer le chemin absolu du fichier (dans le volume Docker)
            Path fileAbsolutePath = Paths.get("/app/uploads/", filePath);
            Resource resource = new FileSystemResource(fileAbsolutePath);

            if (!resource.exists() || !resource.isReadable()) {
                log.error("Fichier non trouvé ou non lisible: {}", fileAbsolutePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Déterminer le type MIME du fichier
            String contentType = Files.probeContentType(fileAbsolutePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Créer les headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));

            log.info("Streaming fichier {} pour candidature {}", filePath, id);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé au téléchargement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Erreur téléchargement fichier candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
