package com.hirehub.candidature.web;

import com.hirehub.candidature.config.UserContext;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.candidature.exceptions.CandidatureChangedStatusException;
import com.hirehub.candidature.exceptions.CandidatureUpdatedException;
import com.hirehub.candidature.exceptions.UnauthorizedException;
import com.hirehub.candidature.repository.HistoriqueStatusRepository;
import com.hirehub.candidature.services.CandidatureService;
import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final HistoriqueStatusRepository historiqueStatusRepository;

    public CandidatureController(CandidatureService candidatureService,
                                 HistoriqueStatusRepository historiqueStatusRepository) {
        this.candidatureService = candidatureService;
        this.historiqueStatusRepository = historiqueStatusRepository;
    }

    /**
     * POST /candidatures
     */
    @PostMapping
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
    public ResponseEntity<ApiResponse<List<Candidature>>> myCandidatures() {

        // TODO: lire le candidatId depuis le SecurityContext
        List<Candidature> data = candidatureService.getMyCandidaturesByCandidat();
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
            UserContext.UserInfo user = UserContext.getUser();
            if (user == null) {
                throw new UnauthorizedException("Authentification requise");
            }
            if (!UserRole.RECRUTEUR.name().equals(user.role)) {
                throw new UnauthorizedException("Accès réservé aux recruteurs");
            }

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
            UserContext.UserInfo user = UserContext.getUser();
            if (user == null) {
                throw new UnauthorizedException("Authentification requise");
            }
            if (!UserRole.CANDIDAT.name().equals(user.role)) {
                throw new UnauthorizedException("Accès réservé aux candidats");
            }

            candidatureService.updateCandidatureDetailsByCandidat(id, cvPath, lettreMotivationPath);
            Candidature updated = candidatureService.getCandidatureById(id);
            // garder les fichiers dans le volume docker
            return ResponseEntity.ok(ApiResponse.ok("Fichiers mis à jour", updated));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur update fichiers candidature {}: {}", id, e.getMessage(), e);
            throw new CandidatureUpdatedException(e.getMessage());
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
            UserContext.UserInfo user = UserContext.getUser();
            if (user == null) {
                throw new UnauthorizedException("Authentification requise");
            }
            if (!UserRole.CANDIDAT.name().equals(user.role)) {
                throw new UnauthorizedException("Accès réservé aux candidats");
            }

            candidatureService.uploadCVAndCoverLetter(id, cvPath, lettreMotivationPath);
            Candidature updated = candidatureService.getCandidatureById(id);
            return ResponseEntity.ok(ApiResponse.ok("Upload enregistré", updated));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur upload fichiers candidature {}: {}", id, e.getMessage(), e);
            throw new CandidatureUpdatedException(e.getMessage());
        }
    }

    /**
     * GET /candidatures/{id}/historique
     */
    @GetMapping("/{id}/historique")
    public ResponseEntity<ApiResponse<List<HistoriqueStatus>>> historique(@PathVariable String id)
            throws Exception {
        try {
            // TODO: vérifier que (candidat propriétaire) OU (recruteur propriétaire de l'offre)
            List<HistoriqueStatus> data = historiqueStatusRepository.findByCandidatureIdOrderByDateChangementDesc(id);
            return ResponseEntity.ok(ApiResponse.ok("Historique récupéré", data));
        } catch (Exception e) {
            log.error("Erreur récupération historique candidature {}: {}", id, e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * GET /candidatures/offre/{offreId}
     */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<ApiResponse<List<Candidature>>> byOffer(@PathVariable String offreId) {
        // TODO: vérifier que le recruteur authentifié est propriétaire de l'offre

        try {
            UserContext.UserInfo user = UserContext.getUser();
            if (user == null) {
                throw new UnauthorizedException("Authentification requise");
            }
            if (!UserRole.RECRUTEUR.name().equals(user.role)) {
                throw new UnauthorizedException("Accès réservé aux recruteurs");
            }

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
            UserContext.UserInfo user = UserContext.getUser();
            if (user == null) {
                throw new UnauthorizedException("Authentification requise");
            }
            if (!UserRole.CANDIDAT.name().equals(user.role)) {
                throw new UnauthorizedException("Accès réservé aux candidats");
            }

            candidatureService.deleteCandidatureByCandidat(id);
            return ResponseEntity.ok(ApiResponse.ok("Candidature supprimée", null));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur suppression candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
