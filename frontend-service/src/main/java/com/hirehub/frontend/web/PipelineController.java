package com.hirehub.frontend.web;

import com.hirehub.frontend.auth.HirehubUserDetails;
import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.entretien.EntretienCreateRequest;
import com.hirehub.frontend.entretien.EntretienFrontendClient;
import com.hirehub.frontend.offre.OffreFrontendClient;
import com.hirehub.frontend.offre.OffreView;
import com.hirehub.frontend.viewmodels.CandidatureViewModel;
import com.hirehub.frontend.viewmodels.PipelineViewModel;
import org.springframework.security.core.Authentication;
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
    private final OffreFrontendClient offreFrontendClient;
    private final EntretienFrontendClient entretienFrontendClient;

    public PipelineController(CandidatureFrontendClient candidatureFrontendClient,
                              OffreFrontendClient offreFrontendClient,
                              EntretienFrontendClient entretienFrontendClient) {
        this.candidatureFrontendClient = candidatureFrontendClient;
        this.offreFrontendClient = offreFrontendClient;
        this.entretienFrontendClient = entretienFrontendClient;
    }

    @GetMapping("/pipeline/{offreId}")
    public String pipeline(@PathVariable String offreId, Model model) {
        try {
            log.info("Récupération du pipeline pour l'offre {}", offreId);

            // Récupérer les détails de l'offre une seule fois pour le header de la page
            OffreView offre = fetchOffre(offreId);

            List<CandidatureDTO> candidaturesDTO = candidatureFrontendClient.getCandidaturesByOffre(offreId);

            List<PipelineViewModel> candidatures = new ArrayList<>();
            for (CandidatureDTO dto : candidaturesDTO) {
                PipelineViewModel vm = PipelineViewModel.fromDTO(dto);
                vm.enrichWithOffre(offre);
                candidatures.add(vm);
            }

            model.addAttribute("candidatures", candidatures);
            model.addAttribute("offreId", offreId);
            model.addAttribute("offre", offre);
            log.info("Affichage de {} candidatures dans le pipeline", candidatures.size());
            return "pages/recruteur/pipeline";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du pipeline", e);
            model.addAttribute("error", "Erreur lors de la récupération du pipeline : " + e.getMessage());
            model.addAttribute("candidatures", List.of());
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

            CandidatureViewModel vm = CandidatureViewModel.fromDTO(candidatureDTO);
            OffreView offre = fetchOffre(candidatureDTO.getOffreId());
            vm.enrichWithOffre(offre);

            model.addAttribute("candidature", vm);
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
        // Intercepter ENTRETIEN → formulaire de planification
        if ("ENTRETIEN".equals(status)) {
            return "redirect:/recruteur/candidature/" + id + "/planifier-entretien";
        }
        try {
            log.info("Changement du statut de la candidature {} vers {}", id, status);
            candidatureFrontendClient.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Statut mis à jour avec succès.");

            CandidatureDTO candidature = candidatureFrontendClient.getCandidature(id).orElse(null);
            if (candidature != null) {
                return "redirect:/recruteur/pipeline/" + candidature.getOffreId();
            }
            return "redirect:/recruteur/offres";
        } catch (Exception e) {
            log.error("Erreur lors du changement de statut", e);
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/recruteur/candidature/" + id;
        }
    }

    @GetMapping("/candidature/{id}/planifier-entretien")
    public String planifierEntretienForm(@PathVariable String id, Model model) {
        try {
            CandidatureDTO dto = candidatureFrontendClient.getCandidature(id).orElse(null);
            if (dto == null) {
                model.addAttribute("error", "Candidature introuvable.");
                return "pages/recruteur/planifier-entretien";
            }
            CandidatureViewModel vm = CandidatureViewModel.fromDTO(dto);
            enrichWithOffre(vm);
            model.addAttribute("candidature", vm);
            return "pages/recruteur/planifier-entretien";
        } catch (Exception e) {
            log.error("Erreur chargement formulaire entretien", e);
            model.addAttribute("error", "Impossible de charger le formulaire.");
            return "pages/recruteur/planifier-entretien";
        }
    }

    @PostMapping("/candidature/{id}/planifier-entretien")
    public String planifierEntretienSubmit(
            @PathVariable String id,
            @RequestParam String dateHeure,
            @RequestParam String type,
            @RequestParam(required = false) String lieu,
            @RequestParam(required = false) String lienVisio,
            @RequestParam(required = false) String notesInternes,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            if (!(auth.getPrincipal() instanceof HirehubUserDetails recruteur)) {
                return "redirect:/login";
            }

            // datetime-local envoie "yyyy-MM-ddTHH:mm" (sans secondes) — on complète
            String normalized = dateHeure.length() == 16 ? dateHeure + ":00" : dateHeure;
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(normalized);

            // Récupérer le candidatId depuis la candidature pour éviter l'appel Feign interne
            CandidatureDTO candidatureDTO = candidatureFrontendClient.getCandidature(id).orElse(null);
            String candidatId = candidatureDTO != null ? candidatureDTO.getCandidatId() : null;

            EntretienCreateRequest req = new EntretienCreateRequest();
            req.setCandidatureId(id);
            req.setCandidatId(candidatId);
            req.setRecruteurId(recruteur.getId().toString());
            req.setDateHeure(dt);
            req.setType(type);
            req.setLieu("PRESENTIEL".equals(type) ? lieu : null);
            req.setLienVisio("VISIO".equals(type) ? lienVisio : null);
            req.setNotesInternes(notesInternes);

            // 1. Créer l'entretien (le service n'appelle plus candidature-service en interne)
            entretienFrontendClient.create(req);

            // 2. Passer le statut de la candidature à ENTRETIEN via le gateway (JWT valide)
            candidatureFrontendClient.updateStatus(id, "ENTRETIEN");

            redirectAttributes.addFlashAttribute("success", "Entretien planifié avec succès.");
            CandidatureDTO dto = candidatureFrontendClient.getCandidature(id).orElse(null);
            if (dto != null) return "redirect:/recruteur/pipeline/" + dto.getOffreId();
            return "redirect:/recruteur/offres";
        } catch (Exception e) {
            log.error("Erreur planification entretien", e);
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/recruteur/candidature/" + id + "/planifier-entretien";
        }
    }

    private void enrichWithOffre(CandidatureViewModel vm) {
        OffreView offre = fetchOffre(vm.getOffreId());
        vm.enrichWithOffre(offre);
    }

    private OffreView fetchOffre(String offreId) {
        if (offreId == null) return null;
        try {
            return offreFrontendClient.detail(Long.parseLong(offreId));
        } catch (NumberFormatException ignored) {
            return null;
        } catch (Exception e) {
            log.warn("Impossible de récupérer l'offre {} : {}", offreId, e.getMessage());
            return null;
        }
    }
}
