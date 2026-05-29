package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.clients.HistoriqueStatutDTO;
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
    // Computed display properties
    private String ancienStatusLabel;
    private String nouveauStatusLabel;
    private String ancienStatusBadgeClass;
    private String nouveauStatusBadgeClass;

    public static HistoriqueStatutViewModel fromDTO(HistoriqueStatutDTO dto) {
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

    /** Retourne le timestamp déjà formaté par le service ("yyyy-MM-dd HH:mm:ss"). */
    public String getDateChangementFormatted() {
        return timestamp != null ? timestamp : "-";
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

    private static String getStatusBadgeClass(String status) {
        if (status == null) return "badge bg-secondary";
        return switch (status) {
            case "SOUMISE"   -> "badge bg-info";
            case "EN_COURS"  -> "badge bg-warning";
            case "ENTRETIEN" -> "badge bg-primary";
            case "ACCEPTEE"  -> "badge bg-success";
            case "REFUSEE"   -> "badge bg-danger";
            default          -> "badge bg-secondary";
        };
    }
}
