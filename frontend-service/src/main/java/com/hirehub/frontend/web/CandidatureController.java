package com.hirehub.frontend.web;

import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.clients.CandidatureServiceClient;
import com.hirehub.frontend.clients.HistoriqueStatutDTO;
import com.hirehub.frontend.viewmodels.CandidatureViewModel;
import com.hirehub.frontend.viewmodels.HistoriqueStatutViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour les routes candidat liées aux candidatures
 * Gère l'affichage et les opérations sur les candidatures
 */
@Controller
@RequestMapping("/candidat")
@Slf4j
public class CandidatureController {

    private final CandidatureServiceClient candidatureServiceClient;

    public CandidatureController(CandidatureServiceClient candidatureServiceClient) {
        this.candidatureServiceClient = candidatureServiceClient;
    }

    /**
     * GET /candidat/mes-candidatures
     * Affiche la liste des candidatures du candidat
     */
    @GetMapping("/mes-candidatures")
    public String myCandidatures(Authentication auth, Model model) {
        try {
            log.info("Récupération des candidatures pour candidat");

            List<CandidatureDTO> candidaturesDTO = candidatureServiceClient.getMyCandidatures();

            // Convertir DTO en ViewModel
            List<CandidatureViewModel> candidatures = new ArrayList<>();
            for (CandidatureDTO dto : candidaturesDTO) {
                candidatures.add(CandidatureViewModel.fromDTO(dto));
            }

            model.addAttribute("candidatures", candidatures);
            log.info("Affichage de {} candidatures", candidatures.size());

            return "pages/candidat/mes-candidatures";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des candidatures", e);
            model.addAttribute("error", "Erreur lors de la récupération de vos candidatures");
            return "pages/candidat/mes-candidatures";
        }
    }

    /**
     * GET /candidat/candidature/{id}
     * Affiche les détails d'une candidature
     */
    @GetMapping("/candidature/{id}")
    public String candidatureDetail(@PathVariable String id, Model model) {
        try {
            log.info("Récupération des détails de la candidature {}", id);

            CandidatureDTO candidatureDTO = candidatureServiceClient.getCandidature(id);
            if (candidatureDTO == null) {
                model.addAttribute("error", "Candidature non trouvée");
                return "pages/candidat/candidature-detail";
            }

            CandidatureViewModel candidature = CandidatureViewModel.fromDTO(candidatureDTO);
            model.addAttribute("candidature", candidature);

            return "pages/candidat/candidature-detail";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la candidature", e);
            model.addAttribute("error", "Erreur lors de la récupération de la candidature");
            return "pages/candidat/candidature-detail";
        }
    }

    /**
     * GET /candidat/candidature/{id}/historique
     * Affiche l'historique des changements de statut
     */
    @GetMapping("/candidature/{id}/historique")
    public String candidatureHistorique(@PathVariable String id, Model model) {
        try {
            log.info("Récupération de l'historique de la candidature {}", id);

            List<HistoriqueStatutDTO> historiqueDTO = candidatureServiceClient.getHistorique(id);

            // Convertir DTO en ViewModel
            List<HistoriqueStatutViewModel> historique = new ArrayList<>();
            for (HistoriqueStatutDTO dto : historiqueDTO) {
                historique.add(HistoriqueStatutViewModel.fromDTO(dto));
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

    /**
     * POST /candidat/candidature/{id}/delete
     * Supprime une candidature
     */
    @PostMapping("/candidature/{id}/delete")
    public String deleteCandidature(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            log.info("Suppression de la candidature {}", id);
            candidatureServiceClient.deleteCandidature(id);
            redirectAttributes.addFlashAttribute("success", "Candidature supprimée avec succès");
            return "redirect:/candidat/mes-candidatures";
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la candidature", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression");
            return "redirect:/candidat/mes-candidatures";
        }
    }

    /**
     * GET /candidat/candidature/{id}/download
     * Télécharge les fichiers d'une candidature
     */
    @GetMapping("/candidature/{id}/download")
    public String downloadFile(
            @PathVariable String id,
            @RequestParam(value = "type", defaultValue = "cv") String fileType,
            Model model) {
        try {
            log.info("Téléchargement du fichier {} pour candidature {}", fileType, id);
            candidatureServiceClient.downloadFile(id, fileType);
            model.addAttribute("success", "Fichier disponible au téléchargement");
            return "pages/candidat/candidature-detail";
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement", e);
            model.addAttribute("error", "Erreur lors du téléchargement");
            return "pages/candidat/candidature-detail";
        }
    }

    /**
     * GET /candidat/postuler/{offreId}
     * Affiche le formulaire de candidature
     */
    @GetMapping("/postuler/{offreId}")
    public String postulerForm(@PathVariable String offreId, Model model) {
        log.info("Affichage du formulaire de candidature pour offre {}", offreId);
        model.addAttribute("offreId", offreId);
        return "pages/candidat/postuler";
    }

    /**
     * POST /candidat/postuler
     * Crée une nouvelle candidature
     */
    @PostMapping("/postuler")
    public String postuler(
            @RequestParam String offreId,
            @RequestParam(required = false) String cvPath,
            @RequestParam(required = false) String lettreMotivationPath,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("Création d'une candidature pour l'offre {}", offreId);

            CandidatureDTO candidatureDTO = new CandidatureDTO();
            candidatureDTO.setOffreId(offreId);
            candidatureDTO.setCvPath(cvPath);
            candidatureDTO.setLettreMotivationPath(lettreMotivationPath);

            CandidatureDTO created = candidatureServiceClient.createCandidature(candidatureDTO);

            redirectAttributes.addFlashAttribute("success", "Candidature créée avec succès!");
            return "redirect:/candidat/mes-candidatures";
        } catch (Exception e) {
            log.error("Erreur lors de la création de la candidature", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de la candidature");
            return "redirect:/candidat/postuler/" + offreId;
        }
    }
}

