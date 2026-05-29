package com.hirehub.frontend.web;

import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.viewmodels.CandidatureViewModel;
import com.hirehub.frontend.viewmodels.PipelineViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/recruteur")
@Slf4j
public class PipelineController {

    private final CandidatureFrontendClient candidatureFrontendClient;

    public PipelineController(CandidatureFrontendClient candidatureFrontendClient) {
        this.candidatureFrontendClient = candidatureFrontendClient;
    }

    @GetMapping("/pipeline/{offreId}")
    public String pipeline(@PathVariable String offreId, Model model) {
        try {
            log.info("Récupération du pipeline pour l'offre {}", offreId);

            List<CandidatureDTO> candidaturesDTO = candidatureFrontendClient.getCandidaturesByOffre(offreId);

            List<PipelineViewModel> candidatures = new ArrayList<>();
            for (CandidatureDTO dto : candidaturesDTO) {
                candidatures.add(PipelineViewModel.fromDTO(dto));
            }

            model.addAttribute("candidatures", candidatures);
            model.addAttribute("offreId", offreId);
            log.info("Affichage de {} candidatures dans le pipeline", candidatures.size());
            return "pages/recruteur/pipeline";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du pipeline", e);
            model.addAttribute("error", "Erreur lors de la récupération du pipeline");
            model.addAttribute("candidatures", java.util.List.of());
            return "pages/recruteur/pipeline";
        }
    }

    @GetMapping("/candidature/{id}")
    public String candidatureDetail(@PathVariable String id, Model model) {
        try {
            log.info("Récupération des détails de la candidature {}", id);

            CandidatureDTO candidatureDTO = candidatureFrontendClient.getCandidature(id).orElse(null);
            if (candidatureDTO == null) {
                model.addAttribute("error", "Candidature non trouvée");
                return "pages/recruteur/candidature-detail";
            }

            model.addAttribute("candidature", CandidatureViewModel.fromDTO(candidatureDTO));
            return "pages/recruteur/candidature-detail";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la candidature", e);
            model.addAttribute("error", "Erreur lors de la récupération de la candidature");
            return "pages/recruteur/candidature-detail";
        }
    }

    @PostMapping("/candidature/{id}/statut")
    public String changeStatus(
            @PathVariable String id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("Changement du statut de la candidature {} vers {}", id, status);

            candidatureFrontendClient.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Statut mis à jour avec succès");

            CandidatureDTO candidature = candidatureFrontendClient.getCandidature(id).orElse(null);
            if (candidature != null) {
                return "redirect:/recruteur/pipeline/" + candidature.getOffreId();
            }

            return "redirect:/recruteur/pipeline";
        } catch (Exception e) {
            log.error("Erreur lors du changement de statut", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors du changement de statut");
            return "redirect:/recruteur/pipeline";
        }
    }

    @GetMapping("/candidature/{id}/download")
    public String downloadFile(
            @PathVariable String id,
            @RequestParam(value = "type", defaultValue = "cv") String fileType,
            Model model) {
        try {
            log.info("Téléchargement du fichier {} pour candidature {}", fileType, id);
            model.addAttribute("success", "Fichier disponible au téléchargement");
            return "pages/recruteur/candidature-detail";
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement", e);
            model.addAttribute("error", "Erreur lors du téléchargement");
            return "pages/recruteur/candidature-detail";
        }
    }
}
