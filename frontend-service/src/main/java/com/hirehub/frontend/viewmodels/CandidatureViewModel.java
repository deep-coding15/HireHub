package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.offre.OffreView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    private String statusLabel;
    private String statusBadgeClass;

    private String candidatEmail;

    public String getDisplayCandidat() {
        return candidatEmail != null && !candidatEmail.isBlank() ? candidatEmail : candidatId;
    }

    // Données enrichies depuis offre-service (peut être null si service indisponible)
    private String offreTitre;
    private String offreDescription;
    private String offreTypeContrat;
    private String offreVille;
    private Double offreSalaire;
    private String offreDateExpiration;
    private String offreStatut;
    private String offreRecruteurEmail;

    public void enrichWithOffre(OffreView offre) {
        if (offre == null) return;
        this.offreTitre = offre.getTitre();
        this.offreDescription = offre.getDescription();
        this.offreTypeContrat = offre.getTypeContrat();
        this.offreVille = offre.getVille();
        this.offreSalaire = offre.getSalaire();
        this.offreDateExpiration = offre.getDateExpiration();
        this.offreStatut = offre.getStatut();
        this.offreRecruteurEmail = offre.getRecruteurEmail();
    }

    public String getDisplayTitre() {
        return offreTitre != null ? offreTitre : "Offre #" + offreId;
    }

    public String getOffreSalaireFormatted() {
        if (offreSalaire == null || offreSalaire == 0) return "Non précisé";
        return String.format("%,.0f €/an", offreSalaire).replace(",", " ");
    }

    public static CandidatureViewModel fromDTO(CandidatureDTO dto) {
        CandidatureViewModel vm = new CandidatureViewModel();
        vm.setId(dto.getId());
        vm.setCandidatId(dto.getCandidatId());
        vm.setCandidatEmail(dto.getCandidatEmail());
        vm.setOffreId(dto.getOffreId());
        vm.setCvPath(dto.getCvPath());
        vm.setLettreMotivationPath(dto.getLettreMotivationPath());
        vm.setStatus(dto.getStatus());
        vm.setDateSoumission(dto.getDateSoumission());
        vm.setDateModification(dto.getDateModification());

        if (dto.getStatus() != null) {
            switch (dto.getStatus()) {
                case "SOUMISE":
                    vm.setStatusLabel("Soumise");
                    vm.setStatusBadgeClass("badge bg-info");
                    break;
                case "EN_COURS":
                    vm.setStatusLabel("En cours");
                    vm.setStatusBadgeClass("badge bg-warning text-dark");
                    break;
                case "ENTRETIEN":
                    vm.setStatusLabel("Entretien");
                    vm.setStatusBadgeClass("badge bg-primary");
                    break;
                case "ACCEPTEE":
                    vm.setStatusLabel("Acceptée");
                    vm.setStatusBadgeClass("badge bg-success");
                    break;
                case "REFUSEE":
                    vm.setStatusLabel("Refusée");
                    vm.setStatusBadgeClass("badge bg-danger");
                    break;
                default:
                    vm.setStatusLabel(dto.getStatus());
                    vm.setStatusBadgeClass("badge bg-secondary");
            }
        }

        return vm;
    }

    public String getDateSoumissionFormatted() {
        if (dateSoumission == null) return "-";
        return dateSoumission.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getDateModificationFormatted() {
        if (dateModification == null) return "-";
        return dateModification.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getOffreDateExpirationFormatted() {
        if (offreDateExpiration == null || offreDateExpiration.isBlank()) return null;
        try {
            String s = offreDateExpiration.length() > 10 ? offreDateExpiration.substring(0, 10) : offreDateExpiration;
            return LocalDate.parse(s).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return offreDateExpiration;
        }
    }

    public boolean hasCv() {
        return cvPath != null && !cvPath.isEmpty();
    }

    public boolean hasLettre() {
        return lettreMotivationPath != null && !lettreMotivationPath.isEmpty();
    }

    public List<String[]> getAllowedTransitions() {
        if (status == null) return List.of();
        return switch (status) {
            case "SOUMISE"   -> List.of(
                    new String[]{"EN_COURS",  "Mettre en cours"},
                    new String[]{"REFUSEE",   "Refuser"});
            case "EN_COURS"  -> List.of(
                    new String[]{"ENTRETIEN", "Planifier entretien"},
                    new String[]{"REFUSEE",   "Refuser"});
            case "ENTRETIEN" -> List.of(
                    new String[]{"ACCEPTEE",  "Accepter"},
                    new String[]{"REFUSEE",   "Refuser"});
            default          -> List.of();
        };
    }

    public boolean isTerminal() {
        return "ACCEPTEE".equals(status) || "REFUSEE".equals(status);
    }
}
