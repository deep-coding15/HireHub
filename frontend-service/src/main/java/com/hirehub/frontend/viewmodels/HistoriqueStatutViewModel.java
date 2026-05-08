package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.clients.HistoriqueStatutDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ViewModel pour afficher l'historique des statuts
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutViewModel {
    private String id;
    private String candidatureId;
    private String ancienStatus;
    private String nouveauStatus;
    private String commentaire;
    private LocalDateTime dateChangement;
    private String utilisateurId;
    private String ancienStatusLabel;
    private String nouveauStatusLabel;
    private String ancienStatusBadgeClass;
    private String nouveauStatusBadgeClass;

    public static HistoriqueStatutViewModel fromDTO(HistoriqueStatutDTO dto) {
        HistoriqueStatutViewModel vm = new HistoriqueStatutViewModel();
        vm.setId(dto.getId());
        vm.setCandidatureId(dto.getCandidatureId());
        vm.setAncienStatus(dto.getAncienStatus());
        vm.setNouveauStatus(dto.getNouveauStatus());
        vm.setCommentaire(dto.getCommentaire());
        vm.setDateChangement(dto.getDateChangement());
        vm.setUtilisateurId(dto.getUtilisateurId());

        vm.setAncienStatusLabel(formatStatus(dto.getAncienStatus()));
        vm.setNouveauStatusLabel(formatStatus(dto.getNouveauStatus()));
        vm.setAncienStatusBadgeClass(getStatusBadgeClass(dto.getAncienStatus()));
        vm.setNouveauStatusBadgeClass(getStatusBadgeClass(dto.getNouveauStatus()));

        return vm;
    }

    public String getDateChangementFormatted() {
        if (dateChangement == null) return "-";
        return dateChangement.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private static String formatStatus(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "EN_COURS" -> "En cours";
            case "ACCEPTÉE" -> "Acceptée";
            case "REJETÉE" -> "Rejetée";
            case "EN_ATTENTE" -> "En attente";
            default -> status;
        };
    }

    private static String getStatusBadgeClass(String status) {
        if (status == null) return "badge bg-secondary";
        return switch (status) {
            case "EN_COURS" -> "badge bg-warning";
            case "ACCEPTÉE" -> "badge bg-success";
            case "REJETÉE" -> "badge bg-danger";
            case "EN_ATTENTE" -> "badge bg-info";
            default -> "badge bg-secondary";
        };
    }
}

