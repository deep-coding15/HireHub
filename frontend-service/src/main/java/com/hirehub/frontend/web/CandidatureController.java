package com.hirehub.frontend.web;

import com.hirehub.frontend.auth.HirehubUserDetails;
import com.hirehub.frontend.candidature.ApplicationUploadService;
import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.candidature.CandidatureServiceException;
import com.hirehub.frontend.candidature.HistoriqueApiItem;
import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.entretien.EntretienFrontendClient;
import com.hirehub.frontend.viewmodels.CandidatureViewModel;
import com.hirehub.frontend.viewmodels.HistoriqueStatutViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/candidat")
@Slf4j
public class CandidatureController {

    private final CandidatureFrontendClient candidatureFrontendClient;
    private final EntretienFrontendClient entretienFrontendClient;
    private final ApplicationUploadService applicationUploadService;

    public CandidatureController(
            CandidatureFrontendClient candidatureFrontendClient,
            EntretienFrontendClient entretienFrontendClient,
            ApplicationUploadService applicationUploadService
    ) {
        this.candidatureFrontendClient = candidatureFrontendClient;
        this.entretienFrontendClient = entretienFrontendClient;
        this.applicationUploadService = applicationUploadService;
    }

    @GetMapping("/dashboard")
    public String candidatDashboard(Authentication auth, Model model) {
        try {
            List<CandidatureViewModel> candidatures = new ArrayList<>();
            List<CandidatureDTO> candidaturesDTO = candidatureFrontendClient.getMyCandidatures();
            for (CandidatureDTO dto : candidaturesDTO) {
                candidatures.add(CandidatureViewModel.fromDTO(dto));
            }
            model.addAttribute("candidatures", candidatures);
            model.addAttribute("candidaturesCount", candidatures.size());

            if (auth != null && auth.getPrincipal() instanceof HirehubUserDetails details) {
                model.addAttribute("entretiensCount", entretienFrontendClient.listForCandidat(details).size());
            } else {
                model.addAttribute("entretiensCount", 0);
            }
            model.addAttribute("apiError", false);
        } catch (Exception e) {
            log.error("Dashboard candidat", e);
            model.addAttribute("candidatures", List.of());
            model.addAttribute("candidaturesCount", 0);
            model.addAttribute("entretiensCount", 0);
            model.addAttribute("apiError", true);
        }
        return "pages/candidat/dashboard";
    }

    @GetMapping("/mes-candidatures")
    public String myCandidatures(Authentication auth, Model model) {
        try {
            log.info("Récupération des candidatures du candidat");

            List<CandidatureDTO> candidaturesDTO = candidatureFrontendClient.getMyCandidatures();

            List<CandidatureViewModel> candidatures = new ArrayList<>();
            for (CandidatureDTO dto : candidaturesDTO) {
                candidatures.add(CandidatureViewModel.fromDTO(dto));
            }

            model.addAttribute("candidatures", candidatures);
            log.info("Affichage de {} candidatures", candidatures.size());
            return "pages/candidat/mes-candidatures";
        } catch (CandidatureServiceException e) {
            log.warn("Mes candidatures: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("candidatures", List.of());
            return "pages/candidat/mes-candidatures";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des candidatures", e);
            model.addAttribute("error", "Impossible de charger vos candidatures pour le moment.");
            model.addAttribute("candidatures", List.of());
            return "pages/candidat/mes-candidatures";
        }
    }

    @GetMapping("/candidature/{id}")
    public String candidatureDetail(@PathVariable String id, Model model) {
        try {
            log.info("Récupération des détails de la candidature {}", id);

            CandidatureDTO candidatureDTO = candidatureFrontendClient.getCandidature(id).orElse(null);
            if (candidatureDTO == null) {
                model.addAttribute("error", "Candidature non trouvée");
                return "pages/candidat/candidature-detail";
            }

            model.addAttribute("candidature", CandidatureViewModel.fromDTO(candidatureDTO));
            return "pages/candidat/candidature-detail";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la candidature", e);
            model.addAttribute("error", "Erreur lors de la récupération de la candidature");
            return "pages/candidat/candidature-detail";
        }
    }

    @GetMapping("/candidature/{id}/historique")
    public String candidatureHistorique(@PathVariable String id, Model model) {
        try {
            log.info("Récupération de l'historique de la candidature {}", id);

            List<HistoriqueApiItem> historiqueDTO = candidatureFrontendClient.getHistorique(id);

            List<HistoriqueStatutViewModel> historique = new ArrayList<>();
            for (HistoriqueApiItem dto : historiqueDTO) {
                historique.add(HistoriqueStatutViewModel.fromApiItem(dto));
            }

            model.addAttribute("historique", historique);
            model.addAttribute("candidatureId", id);
            return "pages/candidat/candidature-historique";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'historique", e);
            model.addAttribute("error", "Erreur lors de la récupération de l'historique");
            return "pages/candidat/candidature-historique";
        }
    }

    @PostMapping("/candidature/{id}/delete")
    public String deleteCandidature(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            log.info("Retrait de la candidature {}", id);
            candidatureFrontendClient.delete(id);
            redirectAttributes.addFlashAttribute("success", "Candidature retirée avec succès");
            return "redirect:/candidat/mes-candidatures";
        } catch (Exception e) {
            log.error("Erreur lors du retrait de la candidature", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors du retrait");
            return "redirect:/candidat/mes-candidatures";
        }
    }

    @GetMapping("/candidature/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String id,
            @RequestParam(value = "type", defaultValue = "cv") String fileType) {
        try {
            log.info("Téléchargement du fichier {} pour candidature {}", fileType, id);

            CandidatureDTO candidature = candidatureFrontendClient.getCandidature(id)
                    .orElseThrow(() -> new CandidatureServiceException("Candidature introuvable"));

            Path filePath = applicationUploadService.resolveDownloadPath(
                    UUID.fromString(candidature.getCandidatId()),
                    candidature.getOffreId(),
                    fileType
            ).orElseThrow(() -> new CandidatureServiceException("Fichier non disponible"));

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new CandidatureServiceException("Fichier illisible");
            }

            String fileName = filePath.getFileName().toString();
            MediaType mediaType = fileName.endsWith(".pdf")
                    ? MediaType.APPLICATION_PDF
                    : MediaType.TEXT_PLAIN;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/postuler/{offreId}")
    public String postulerForm(@PathVariable String offreId, Model model) {
        log.info("Affichage du formulaire de candidature pour offre {}", offreId);
        model.addAttribute("offreId", offreId);
        return "pages/candidat/postuler";
    }

    @PostMapping("/postuler")
    public String postuler(
            @RequestParam String offreId,
            @RequestParam(required = false) String cvPath,
            @RequestParam(required = false) String lettreMotivationPath,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("Création d'une candidature pour l'offre {}", offreId);

            if (!(org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal() instanceof HirehubUserDetails details)) {
                return "redirect:/login";
            }
            candidatureFrontendClient.create(
                    offreId,
                    cvPath != null ? cvPath : "pending/cv.pdf",
                    lettreMotivationPath != null ? lettreMotivationPath : "pending/lettre.txt",
                    details
            );

            redirectAttributes.addFlashAttribute("success", "Candidature envoyée avec succès.");
            return "redirect:/candidat/mes-candidatures";
        } catch (Exception e) {
            log.error("Erreur lors de la création de la candidature", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de la candidature");
            return "redirect:/candidat/postuler/" + offreId;
        }
    }
}
