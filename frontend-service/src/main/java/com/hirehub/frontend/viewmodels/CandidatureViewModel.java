package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.clients.CandidatureDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ViewModel pour afficher une candidature dans les templates Thymeleaf
 * Enrichit le DTO avec des données formatées pour l'affichage
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureViewModel {
    private String id;
    private String candidatId;
    private String offreId;
    private String cvPath;
    private String lettreMotivationPath;
    private String status;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateModification;

    // Propriétés calculées pour l'affichage
    private String statusLabel;
    private String statusBadgeClass; // Pour CSS Bootstrap

    /**
     * Crée un ViewModel à partir d'un DTO
     */
    public static CandidatureViewModel fromDTO(CandidatureDTO dto) {
        CandidatureViewModel vm = new CandidatureViewModel();
        vm.setId(dto.getId());
        vm.setCandidatId(dto.getCandidatId());
        vm.setOffreId(dto.getOffreId());
        vm.setCvPath(dto.getCvPath());
        vm.setLettreMotivationPath(dto.getLettreMotivationPath());
        vm.setStatus(dto.getStatus());
        vm.setDateSoumission(dto.getDateSoumission());
        vm.setDateModification(dto.getDateModification());

        // Définir le label et la classe CSS du statut
        if (dto.getStatus() != null) {
            switch (dto.getStatus()) {
                case "EN_COURS":
                    vm.setStatusLabel("En cours");
                    vm.setStatusBadgeClass("badge bg-warning");
                    break;
                case "ACCEPTÉE":
                    vm.setStatusLabel("Acceptée");
                    vm.setStatusBadgeClass("badge bg-success");
                    break;
                case "REJETÉE":
                    vm.setStatusLabel("Rejetée");
                    vm.setStatusBadgeClass("badge bg-danger");
                    break;
                case "EN_ATTENTE":
                    vm.setStatusLabel("En attente");
                    vm.setStatusBadgeClass("badge bg-info");
                    break;
                default:
                    vm.setStatusLabel(dto.getStatus());
                    vm.setStatusBadgeClass("badge bg-secondary");
            }
        }

        return vm;
    }

    /**
     * Retourne la date de soumission formatée pour l'affichage
     */
    public String getDateSoumissionFormatted() {
        if (dateSoumission == null) return "-";
        return dateSoumission.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Retourne la date de modification formatée pour l'affichage
     */
    public String getDateModificationFormatted() {
        if (dateModification == null) return "-";
        return dateModification.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Vérifie si un fichier existe (CV ou lettre)
     */
    public boolean hasCv() {
        return cvPath != null && !cvPath.isEmpty();
    }

    public boolean hasLettre() {
        return lettreMotivationPath != null && !lettreMotivationPath.isEmpty();
    }
}

