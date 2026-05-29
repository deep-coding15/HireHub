package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.candidature.HistoriqueApiItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutViewModel {
    private String ancienStatus;
    private String nouveauStatus;
    private String auteur;
    private String commentaire;
    private String timestamp;
    private String ancienStatusLabel;
    private String nouveauStatusLabel;
    private String ancienStatusBadgeClass;
    private String nouveauStatusBadgeClass;

    public static HistoriqueStatutViewModel fromApiItem(HistoriqueApiItem item) {
        HistoriqueStatutViewModel vm = new HistoriqueStatutViewModel();
        vm.setAncienStatus(item.getAncienStatut());
        vm.setNouveauStatus(item.getNouveauStatut());
        vm.setCommentaire(item.getCommentaire());
        vm.setAuteur(item.getAuteur());
        vm.setTimestamp(item.getTimestamp());
        vm.setAncienStatusLabel(item.getAncienStatut() != null ? item.getAncienStatut() : "-");
        vm.setNouveauStatusLabel(item.getNouveauStatut() != null ? item.getNouveauStatut() : "-");
        vm.setAncienStatusBadgeClass(getStatusBadgeClass(item.getAncienStatut()));
        vm.setNouveauStatusBadgeClass(getStatusBadgeClass(item.getNouveauStatut()));
        return vm;
    }

    public static HistoriqueStatutViewModel fromDTO(com.hirehub.frontend.clients.HistoriqueStatutDTO dto) {
        HistoriqueStatutViewModel vm = new HistoriqueStatutViewModel();
        vm.setAncienStatus(dto.getAncienStatut());
        vm.setNouveauStatus(dto.getNouveauStatut());
        vm.setAuteur(dto.getAuteur());
        vm.setCommentaire(dto.getCommentaire());
        vm.setTimestamp(dto.getTimestamp());
        vm.setAncienStatusLabel(formatStatus(dto.getAncienStatut()));
        vm.setNouveauStatusLabel(formatStatus(dto.getNouveauStatut()));
        vm.setAncienStatusBadgeClass(getStatusBadgeClass(dto.getAncienStatut()));
        vm.setNouveauStatusBadgeClass(getStatusBadgeClass(dto.getNouveauStatut()));
        return vm;
    }

    public String getDateChangementFormatted() {
        return timestamp != null ? timestamp : "-";
    }

    private static String getStatusBadgeClass(String status) {
        if (status == null) return "badge bg-secondary";
        String normalized = status.toLowerCase();
        if (normalized.contains("accept") || normalized.contains("retenu")) return "badge bg-success";
        if (normalized.contains("refus") || normalized.contains("rejet")) return "badge bg-danger";
        if (normalized.contains("entretien")) return "badge bg-info";
        if (normalized.contains("cours") || normalized.contains("soumise")) return "badge bg-warning text-dark";
        return "badge bg-secondary";
    }

    private static String formatStatus(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "SOUMISE"   -> "Soumise";
            case "EN_COURS"  -> "En cours";
            case "ENTRETIEN" -> "Entretien";
            case "ACCEPTEE"  -> "Acceptée";
            case "REFUSEE"   -> "Refusée";
            default          -> status;
        };
    }
}
