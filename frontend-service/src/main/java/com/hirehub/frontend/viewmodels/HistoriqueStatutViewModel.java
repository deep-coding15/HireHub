package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.candidature.HistoriqueApiItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    private static final DateTimeFormatter API_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static HistoriqueStatutViewModel fromApiItem(HistoriqueApiItem item) {
        HistoriqueStatutViewModel vm = new HistoriqueStatutViewModel();
        vm.setAncienStatus(item.getAncienStatut());
        vm.setNouveauStatus(item.getNouveauStatut());
        vm.setCommentaire(item.getCommentaire());
        vm.setUtilisateurId(item.getAuteur());
        if (item.getTimestamp() != null) {
            try {
                vm.setDateChangement(LocalDateTime.parse(item.getTimestamp(), API_FORMAT));
            } catch (DateTimeParseException ignored) {
                vm.setDateChangement(null);
            }
        }
        vm.setAncienStatusLabel(item.getAncienStatut() != null ? item.getAncienStatut() : "-");
        vm.setNouveauStatusLabel(item.getNouveauStatut() != null ? item.getNouveauStatut() : "-");
        vm.setAncienStatusBadgeClass(getStatusBadgeClass(item.getAncienStatut()));
        vm.setNouveauStatusBadgeClass(getStatusBadgeClass(item.getNouveauStatut()));
        return vm;
    }

    public static HistoriqueStatutViewModel fromDTO(com.hirehub.frontend.clients.HistoriqueStatutDTO dto) {
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

    private static String getStatusBadgeClass(String status) {
        if (status == null) return "badge bg-secondary";
        String normalized = status.toLowerCase();
        if (normalized.contains("accept") || normalized.contains("retenu")) {
            return "badge bg-success";
        }
        if (normalized.contains("refus") || normalized.contains("rejet")) {
            return "badge bg-danger";
        }
        if (normalized.contains("entretien")) {
            return "badge bg-info";
        }
        if (normalized.contains("cours") || normalized.contains("soumise")) {
            return "badge bg-warning text-dark";
        }
        return "badge bg-secondary";
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
}

